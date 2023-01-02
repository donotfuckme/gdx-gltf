package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Scaling;
import net.mgsx.gltf.demo.GLTFDemo;
import net.mgsx.gltf.demo.GLTFDemo.ShaderMode;
import net.mgsx.gltf.demo.data.ModelEntry;
import net.mgsx.gltf.demo.events.FileOpenEvent;
import net.mgsx.gltf.demo.events.FileSaveEvent;
import net.mgsx.gltf.demo.events.IBLFolderChangeEvent;
import net.mgsx.gltf.demo.events.ModelSelectedEvent;
import net.mgsx.gltf.demo.model.IBLStudio.IBLPreset;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRHDRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRIridescenceAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRVolumeAttribute;
import net.mgsx.gltf.scene3d.model.NodePartPlus;
import net.mgsx.gltf.scene3d.model.NodePlus;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneModel;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class GLTFDemoUI extends Table {

  public static FileSelector fileSelector = null;

  public static boolean LIVE_STATS = true;

  private SelectBox<ModelEntry> entrySelector;
  private SelectBox<String> variantSelector;
  public SelectBox<String> animationSelector;
  public Table screenshotsTable;
  public Slider debugAmbiantSlider;
  public Slider ambiantSlider;
  public Slider lightSlider;
  public Slider debugSpecularSlider;
  public SelectBox<String> cameraSelector;

  public final Vector3UI lightDirectionControl;
  public SelectBox<ShaderMode> shaderSelector;
  private final SelectBox<String> materialSelector;
  private final Table materialTable;

  private final ObjectMap<String, Material> materialMap = new ObjectMap<>();
  private final ObjectMap<String, Node> nodeMap = new ObjectMap<>();
  private final SelectBox<String> nodeSelector;
  private final Table nodeTable;
  private final TextButton btNodeExclusive;
  private Node selectedNode;
  protected CollapsableUI shaderOptions;
  public SelectBox<SRGB> shaderSRGB;
  public BooleanUI shaderGammaCorrection;
  private final CollapsableUI lightOptions;
  public SelectBox<SceneModel> sceneSelector;
  public SelectBox<String> lightSelector;
  public Label shaderCount;
  public TextButton skeletonButton;
  public BooleanUI lightShadow;
  public FloatUI shadowBias;
  public TextButton btAllAnimations;
  public BooleanUI fogEnabled;
  public BooleanUI skyBoxEnabled;
  public SelectBox<SRGB> skyboxSRGB;
  public BooleanUI skyboxGammaCorrection;
  public FloatUI envRotation;
  public BooleanUI transmissionPassEnabled;
  public Vector4UI fogColor;
  public Vector3UI fogEquation;

  public BooleanUI IBLEnabled;

  private Table IBLChooser;

  private CollapsableUI IBLOptions;

  public BooleanUI IBLSpecular;

  public BooleanUI IBLLookup;

  public Vector4UI skyBoxColor;

  public BooleanUI outlinesEnabled;

  public FloatUI outlinesWidth;

  public FloatUI outlineDepthMin;

  public FloatUI outlineDepthMax;

  public Vector4UI outlineInnerColor;

  public Vector4UI outlineOuterColor;

  private final CollapsableUI outlineOptions;

  public FloatUI outlineDistFalloff;

  public BooleanUI outlineDistFalloffOption;

  private final Label lightLabel;

  private final SceneManager sceneManager;

  public final SelectBox<IBLPreset> IBLSelector;

  private final FileHandle rootFolder;

  public final Slider uiScaleSlider;

  public final Slider emissiveSlider;

  public final SelectBox<SRGB> transmissionSRGB;

  public GLTFDemoUI(SceneManager sceneManager, Skin skin, final FileHandle rootFolder) {
    super(skin);
    this.sceneManager = sceneManager;
    this.rootFolder = rootFolder;

    Table root = new Table(skin);
    root.defaults().pad(5);
    Table rootRight = new Table(skin);
    rootRight.defaults().pad(5);

    add(root).expandY().top();
    add().expand();
    add(rootRight).expandY().top();

    if (fileSelector != null) {
      TextButton btOpenFile = new TextButton("Open file", skin);
      TextButton btExportFile = new TextButton("Save to file", skin);
      root.add("File");
      Table btTable = new Table();
      btTable.add(btOpenFile);
      btTable.add(btExportFile);
      root.add(btTable).row();

      btOpenFile.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          fileSelector.open(new Runnable() {
            @Override
            public void run() {
              GLTFDemoUI.this.fire(new FileOpenEvent(fileSelector.lastFile));
            }
          });
        }
      });

      btExportFile.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          fileSelector.open(new Runnable() {
            @Override
            public void run() {
              GLTFDemoUI.this.fire(new FileSaveEvent(fileSelector.lastFile));
            }
          });
        }
      });
    }

    if (rootFolder != null) {
      entrySelector = new SelectBox<>(skin);
      root.add("Model");
      root.add(entrySelector).row();

      root.add("Screenshot");
      screenshotsTable = new Table(skin);
      root.add(screenshotsTable).row();

      variantSelector = new SelectBox<>(skin);
      root.add("File");
      root.add(variantSelector).row();

      entrySelector.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          setEntry(entrySelector.getSelected(), rootFolder);
          setImage(entrySelector.getSelected());
        }
      });

      variantSelector.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          load(entrySelector.getSelected(), variantSelector.getSelected());
        }
      });
    }

    // scene
    sceneSelector = new SelectBox<SceneModel>(skin) {
      @Override
      protected String toString(SceneModel item) {
        return item.name == null ? "<null>" : item.name;
      }
    };
    root.add("Scene");
    root.add(sceneSelector).row();

    // Shader options
    shaderSelector = new SelectBox<>(skin);
    root.add("Shader");
    root.add(shaderSelector).row();
    shaderSelector.setItems(ShaderMode.values());

    root.add("Shader count");
    shaderCount = new Label("", skin);
    root.add(shaderCount).row();

    root.add();
    shaderOptions = new CollapsableUI(skin, "Shader Options", false);
    root.add(shaderOptions).row();
    shaderOptions.optTable.add("SRGB");
    shaderSRGB = new SelectBox<>(skin);
    shaderOptions.optTable.add(shaderSRGB).row();
    shaderSRGB.setItems(SRGB.values());
    shaderSRGB.setSelected(SRGB.ACCURATE);

    shaderOptions.optTable.add("Gamma correction");
    shaderGammaCorrection = new BooleanUI(skin, true);
    shaderOptions.optTable.add(shaderGammaCorrection).row();

    // Fog
    shaderOptions.optTable.add("Fog");
    fogEnabled = new BooleanUI(skin, false);
    shaderOptions.optTable.add(fogEnabled).row();

    shaderOptions.optTable.add("Fog Color");
    fogColor = new Vector4UI(skin, new Color());
    shaderOptions.optTable.add(fogColor).row();

    shaderOptions.optTable.add("Fog Equation");
    fogEquation = new Vector3UI(skin, new Vector3(-1f, 1f, -0.8f));
    shaderOptions.optTable.add(fogEquation).row();

    // Skybox
    shaderOptions.optTable.add("SkyBox");
    skyBoxEnabled = new BooleanUI(skin, true);
    shaderOptions.optTable.add(skyBoxEnabled).row();

    shaderOptions.optTable.add("SkyBox Color");
    skyBoxColor = new Vector4UI(skin, new Color(Color.WHITE));
    shaderOptions.optTable.add(skyBoxColor).row();

    shaderOptions.optTable.add("Skybox SRGB");
    skyboxSRGB = new SelectBox<>(skin);
    shaderOptions.optTable.add(skyboxSRGB).row();
    skyboxSRGB.setItems(SRGB.values());
    skyboxSRGB.setSelected(SRGB.NONE);

    shaderOptions.optTable.add("Skybox Gamma");
    skyboxGammaCorrection = new BooleanUI(skin, false);
    shaderOptions.optTable.add(skyboxGammaCorrection).row();

    shaderOptions.optTable.add("Rotation");
    envRotation = new FloatUI(skin, 0.5f);
    shaderOptions.optTable.add(envRotation).row();

    shaderOptions.optTable.add("Transmission Pass");
    transmissionPassEnabled = new BooleanUI(skin, true);
    shaderOptions.optTable.add(transmissionPassEnabled).row();

    shaderOptions.optTable.add("Transmission SRGB");
    transmissionSRGB = new SelectBox<>(skin);
    shaderOptions.optTable.add(transmissionSRGB).row();
    transmissionSRGB.setItems(SRGB.values());
    transmissionSRGB.setSelected(SRGB.ACCURATE);

    // Outlines
    root.add();
    outlineOptions = new CollapsableUI(skin, "Outline Options", false);
    root.add(outlineOptions).row();

    outlineOptions.optTable.add("Outlines");
    outlinesEnabled = new BooleanUI(skin, false);
    outlineOptions.optTable.add(outlinesEnabled).row();

    outlineOptions.optTable.add("Thickness");
    outlinesWidth = new FloatUI(skin, 0f);
    outlineOptions.optTable.add(outlinesWidth).row();

    outlineOptions.optTable.add("Depth min");
    outlineDepthMin = new FloatUI(skin, .35f);
    outlineOptions.optTable.add(outlineDepthMin).row();
    outlineOptions.optTable.add("Depth max");
    outlineDepthMax = new FloatUI(skin, .9f);
    outlineOptions.optTable.add(outlineDepthMax).row();

    outlineOptions.optTable.add("Inner color");
    outlineInnerColor = new Vector4UI(skin, new Color(0, 0, 0, .3f));
    outlineOptions.optTable.add(outlineInnerColor).row();

    outlineOptions.optTable.add("Outer color");
    outlineOuterColor = new Vector4UI(skin, new Color(0, 0, 0, .7f));
    outlineOptions.optTable.add(outlineOuterColor).row();

    outlineOptions.optTable.add("Distance Falloff");
    outlineDistFalloffOption = new BooleanUI(skin, false);
    outlineOptions.optTable.add(outlineDistFalloffOption).row();

    outlineOptions.optTable.add("Distance Exponent");
    outlineDistFalloff = new FloatUI(skin, 1f);
    outlineOptions.optTable.add(outlineDistFalloff).row();

    // Lighting options
    root.add();
    lightOptions = new CollapsableUI(skin, "Light Options", false);
    root.add(lightOptions).row();

    lightSlider = new Slider(0, 100, .01f, false, skin);
    lightOptions.optTable.add("Light intensity");
    lightOptions.optTable.add(lightSlider).row();
    lightSlider.setValue(1f);

    lightOptions.optTable.add("Dir Light");
    lightDirectionControl = new Vector3UI(skin, new Vector3());
    lightOptions.optTable.add(lightDirectionControl).row();

    lightOptions.optTable.add("Shadows");
    lightShadow = new BooleanUI(skin, false);
    lightOptions.optTable.add(lightShadow).row();

    lightOptions.optTable.add("Shadow Bias");
    shadowBias = new FloatUI(skin, 0);
    lightOptions.optTable.add(shadowBias).row();

    ambiantSlider = new Slider(0, 1, .01f, false, skin);
    ambiantSlider.setValue(1f);
    lightOptions.optTable.add("Ambient Light");
    lightOptions.optTable.add(ambiantSlider).row();

    emissiveSlider = new Slider(0, 10, .1f, false, skin);
    emissiveSlider.setValue(1f);
    lightOptions.optTable.add("Emissive intensity");
    lightOptions.optTable.add(emissiveSlider).row();

    // IBL options
    root.add();
    IBLOptions = new CollapsableUI(skin, "IBL Options", false);
    root.add(IBLOptions).row();

    IBLOptions.optTable.add("Overall");
    IBLEnabled = new BooleanUI(skin, true);
    IBLOptions.optTable.add(IBLEnabled).row();

    IBLOptions.optTable.add("Radiance");
    IBLSpecular = new BooleanUI(skin, true);
    IBLOptions.optTable.add(IBLSpecular).row();

    IBLOptions.optTable.add("BSDF");
    IBLLookup = new BooleanUI(skin, true);
    IBLOptions.optTable.add(IBLLookup).row();

    IBLOptions.optTable.add("Presets");
    IBLChooser = new Table(skin);
    IBLOptions.optTable.add(IBLChooser).row();

    IBLSelector = new SelectBox<>(getSkin());
    IBLChooser.add(IBLSelector);
    if (fileSelector != null) {
      TextButton btOpenFile = new TextButton("Load", skin);
      IBLChooser.add(btOpenFile);

      btOpenFile.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          fileSelector.selectFolder(new Runnable() {
            @Override
            public void run() {
              GLTFDemoUI.this.fire(new IBLFolderChangeEvent(fileSelector.lastFile));
            }
          });
        }
      });
    }

    // Scene options
    cameraSelector = new SelectBox<>(skin);
    root.add("Camera");
    root.add(cameraSelector).row();

    lightSelector = new SelectBox<>(skin);
    lightLabel = new Label("Lights", skin);
    root.add(lightLabel);
    root.add(lightSelector).row();

    animationSelector = new SelectBox<>(skin);
    btAllAnimations = new TextButton("All", skin, "toggle");
    root.add("Animation");
    root.add(animationSelector);
    root.add(btAllAnimations).row();

    skeletonButton = new TextButton("Skeletons", getSkin(), "toggle");
    root.add("Skeletons");
    root.add(skeletonButton).row();

    rootRight.add("UI Scale");
    uiScaleSlider = new Slider(1, 3, .5f, false, getSkin());
    uiScaleSlider.setValue(GLTFDemo.defaultUIScale);
    rootRight.add(uiScaleSlider).row();

    materialSelector = new SelectBox<>(skin);
    rootRight.add("Materials");
    rootRight.add(materialSelector).row();

    materialTable = new Table(skin);
    rootRight.add();
    rootRight.add(materialTable).row();

    nodeSelector = new SelectBox<>(skin);
    rootRight.add("Nodes");
    rootRight.add(nodeSelector).row();
    rootRight.add();
    btNodeExclusive = new TextButton("Hide other nodes", getSkin(), "toggle");
    rootRight.add(btNodeExclusive).row();

    nodeTable = new Table(skin);
    rootRight.add();
    rootRight.add(nodeTable).row();

    materialSelector.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        setMaterial(materialSelector.getSelected());
      }
    });

    nodeSelector.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        setNode(nodeSelector.getSelected());
      }
    });

    btNodeExclusive.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        showHideOtherNodes(btNodeExclusive.isChecked());
      }
    });

    btAllAnimations.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {

      }
    });
  }

  private void load(ModelEntry entry, String variant) {
    if (variant.isEmpty()) return;

    final String fileName = entry.variants.get(variant);
    if (fileName == null) return;

    FileHandle baseFolder = rootFolder.child(entry.name).child(variant);
    FileHandle glFile = baseFolder.child(fileName);

    fire(new ModelSelectedEvent(glFile));
  }

  protected void setImage(ModelEntry entry) {
    if (entry.screenshot != null) {
      FileHandle file = rootFolder.child(entry.name).child(entry.screenshot);
      if (file.exists()) {
        setImage(new Texture(file));
      } else {
        Gdx.app.error("DEMO UI", "file not found " + file.path());
      }
    }
  }

  protected TextButton toggle(String name, boolean checked) {
    TextButton bt = new TextButton(name, getSkin(), "toggle");
    bt.setChecked(checked);
    return bt;
  }

  protected void showHideOtherNodes(boolean hide) {
    for (Entry<String, Node> entry : nodeMap) {
      for (NodePart part : entry.value.parts) {
        part.enabled = selectedNode == null || !hide || entry.value == selectedNode;
      }
    }
  }

  protected void setNode(String nodeID) {
    nodeTable.clearChildren();
    selectedNode = null;
    if (nodeID == null || nodeID.isEmpty()) {
      showHideOtherNodes(false);
      return;
    }

    final Node node = nodeMap.get(nodeID);

    selectedNode = node;

    showHideOtherNodes(btNodeExclusive.isChecked());

    if (node instanceof NodePlus) {
      final NodePlus np = (NodePlus) node;
      if (np.weights != null) {
        WeightsUI weightEditor = new WeightsUI(getSkin(), np.weights, np.morphTargetNames);
        nodeTable.add("Morph Targets").row();
        nodeTable.add(weightEditor);
        weightEditor.addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            for (NodePart part : np.parts) {
              NodePartPlus npp = (NodePartPlus) part;
              if (npp.morphTargets != null) {
                npp.morphTargets.set(np.weights);
              }
            }
          }
        });
      }
    }
  }

  protected void setMaterial(String materialID) {
    materialTable.clearChildren();
    if (materialID == null || materialID.isEmpty()) return;

    final Material material = materialMap.get(materialID);

    // base color
    materialTable.add(new ColorAttributeUI(getSkin(), material.get(ColorAttribute.class, PBRColorAttribute.BaseColorFactor))).row();
    addMaterialTextureSwitch("Color Texture", material, PBRTextureAttribute.BaseColorTexture);

    // emissive
    materialTable.add(new ColorAttributeUI(getSkin(), material.get(ColorAttribute.class, PBRColorAttribute.Emissive))).row();
    addMaterialTextureSwitch("Emissive Texture", material, PBRTextureAttribute.EmissiveTexture);

    // metallic roughness
    materialTable.add(new FloatAttributeUI(getSkin(), material.get(PBRFloatAttribute.class, PBRFloatAttribute.Metallic))).row();
    materialTable.add(new FloatAttributeUI(getSkin(), material.get(PBRFloatAttribute.class, PBRFloatAttribute.Roughness))).row();
    addMaterialTextureSwitch("MR Texture", material, PBRTextureAttribute.MetallicRoughnessTexture);

    // normal
    materialTable.add(new FloatAttributeUI(getSkin(), material.get(PBRFloatAttribute.class, PBRFloatAttribute.NormalScale))).row();
    addMaterialTextureSwitch("Normal Texture", material, PBRTextureAttribute.NormalTexture);

    // occlusion
    materialTable.add(new FloatAttributeUI(getSkin(), material.get(PBRFloatAttribute.class, PBRFloatAttribute.OcclusionStrength))).row();
    addMaterialTextureSwitch("Occlusion Texture", material, PBRTextureAttribute.OcclusionTexture);

    // transmission
    materialTable.add(new FloatAttributeUI(getSkin(), material.get(PBRFloatAttribute.class, PBRFloatAttribute.TransmissionFactor))).row();
    addMaterialTextureSwitch("Transmission Texture", material, PBRTextureAttribute.TransmissionTexture);

    // volume
    final PBRVolumeAttribute volume = material.get(PBRVolumeAttribute.class, PBRVolumeAttribute.Type);
    if (volume != null) {
      materialTable.add(new FloatUI(getSkin(), volume.thicknessFactor, "Thickness", 0, 10) {
        @Override
        protected void onChange(float value) {
          volume.thicknessFactor = value;
        }
      }).row();
    }
    addMaterialTextureSwitch("Thickness Texture", material, PBRTextureAttribute.ThicknessTexture);

    materialTable.add(new FloatAttributeUI(getSkin(), material.get(PBRFloatAttribute.class, PBRFloatAttribute.IOR), 1f, 3f)).row();

    // Specular
    materialTable.add(new FloatAttributeUI(getSkin(), material.get(PBRFloatAttribute.class, PBRFloatAttribute.SpecularFactor), 0, 1)).row();
    addMaterialTextureSwitch("Specular Factor Texture", material, PBRTextureAttribute.SpecularFactorTexture);
    materialTable.add(new HDRColorAttributeUI(getSkin(), material.get(PBRHDRColorAttribute.class, PBRHDRColorAttribute.Specular), 100f)).row();
    addMaterialTextureSwitch("Specular Color Texture", material, PBRTextureAttribute.Specular);

    // Iridescence
    final PBRIridescenceAttribute iridescence = material.get(PBRIridescenceAttribute.class, PBRIridescenceAttribute.Type);
    if (iridescence != null) {
      materialTable.add(new FloatUI(getSkin(), iridescence.factor, "Iridescence") {
        @Override
        protected void onChange(float value) {
          iridescence.factor = value;
        }
      }).row();
    }
  }

  private void addMaterialTextureSwitch(String name, final Material material, long type) {
    final PBRTextureAttribute attribute = material.get(PBRTextureAttribute.class, type);
    if (attribute != null) {
      final TextButton bt = new TextButton(name, getSkin(), "toggle");
      bt.setChecked(true);
      materialTable.add(bt);

      Image pict = new Image(attribute.textureDescription.texture);

      pict.setScaling(Scaling.fit);

      materialTable.add(pict).size(64);

      materialTable.row();

      bt.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (bt.isChecked()) {
            material.set(attribute);
          } else {
            material.remove(attribute.type);
          }
        }
      });
    }

  }

  public void setEntry(ModelEntry entry, FileHandle rootFolder) {
    variantSelector.setSelected(null);
    Array<String> variants = entry.variants.keys().toArray(new Array<String>());
    variants.insert(0, "");
    variantSelector.setItems(variants);

    if (entry.variants.size == 1) {
      variantSelector.setSelectedIndex(1);
    } else {
      variantSelector.setSelectedIndex(0);
    }

    if (screenshotsTable.getChildren().size > 0) {
      Image imgScreenshot = (Image) screenshotsTable.getChildren().first();
      ((TextureRegionDrawable) imgScreenshot.getDrawable()).getRegion().getTexture().dispose();
    }
    screenshotsTable.clear();
  }

  public void setImage(Texture texture) {
    if (texture != null) {
      Image img = new Image(texture);
      img.setScaling(Scaling.fit);
      screenshotsTable.add(img).height(100);
    }
  }

  public void setAnimations(Array<Animation> animations) {
    if (animations.size > 0) {
      Array<String> ids = new Array<>();
      ids.add("");
      for (Animation anim : animations) {
        ids.add(anim.id);
      }
      animationSelector.setItems(ids);
    } else {
      animationSelector.setItems();
    }
  }

  public void setCameras(ObjectMap<Node, Camera> cameras) {
    Array<String> cameraNames = new Array<>();
    cameraNames.add("");
    for (Entry<Node, Camera> e : cameras) {
      cameraNames.add(e.key.id);
    }
    cameraSelector.setItems();
    cameraSelector.setItems(cameraNames);
  }

  public void setMaterials(Array<Material> materials) {
    materialMap.clear();

    Array<String> names = new Array<>();
    names.add("");
    for (Material e : materials) {
      names.add(e.id);
      materialMap.put(e.id, e);
    }
    materialSelector.setItems();
    materialSelector.setItems(names);
  }

  public void setNodes(Array<Node> nodes) {
    nodeMap.clear();

    Array<String> names = new Array<>();
    for (Node e : nodes) {
      names.add(e.id);
      nodeMap.put(e.id, e);
    }
    names.sort();
    names.insert(0, "");
    nodeSelector.setItems();
    nodeSelector.setItems(names);
  }

  public void setScenes(Array<SceneModel> scenes) {
    if (scenes == null) {
      sceneSelector.setDisabled(true);
      sceneSelector.setItems(new Array<SceneModel>());
    } else {
      sceneSelector.setDisabled(false);
      Array<SceneModel> items = new Array<>();
      for (int i = 0; i < scenes.size; i++) {
        items.add(scenes.get(i));
      }
      sceneSelector.setItems(items);
    }
  }

  public void setLights(ObjectMap<Node, BaseLight<?>> lights) {
    Array<String> names = new Array<>();
    names.add("");
    for (Entry<Node, BaseLight<?>> entry : lights) {
      names.add(entry.key.id);
    }
    lightSelector.setItems(names);
  }

  @Override
  public void act(float delta) {
    if (LIVE_STATS) {
      lightLabel.setText("Lights (" + sceneManager.getActiveLightsCount() + "/" + sceneManager.getTotalLightsCount() + ")");
    }
    super.act(delta);
  }

  public void toggleModelSelector(boolean state) {

  }

  public void setEntries(Array<ModelEntry> entries, String defaultEntry, String defaultVariant) {
    entrySelector.setItems(entries);

    if (defaultEntry != null && defaultVariant != null) {
      for (int i = 0; i < entries.size; i++) {
        ModelEntry entry = entries.get(i);
        if (entry.name.equals(defaultEntry)) {
          entrySelector.setSelected(entry);
          // will be auto select if there is only one variant.
          if (entry.variants.size != 1) {
            variantSelector.setSelected(defaultVariant);
          }
          break;
        }
      }
    }
  }
}
