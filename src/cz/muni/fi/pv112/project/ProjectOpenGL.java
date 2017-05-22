package cz.muni.fi.pv112.project;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGR;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL14.GL_MIRRORED_REPEAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import sun.applet.Main;

public class ProjectOpenGL {
    
    private static SoundThread soundThread;
    private static Thread threadS;
    private static final float QUAD[] =
    {
            //       Position                 Normal             Tex Coord
            // Front face
            -1.0f, 1.0f, 0.0f,		 0.0f, 0.0f, 1.0f,		0.0f, 1.0f,
            -1.0f,-1.0f, 0.0f,		 0.0f, 0.0f, 1.0f,		0.0f, 0.0f,
            1.0f,-1.0f, 0.0f,		 0.0f, 0.0f, 1.0f,		1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f,		 0.0f, 0.0f, 1.0f,		0.0f, 1.0f,
            1.0f,-1.0f, 0.0f,		 0.0f, 0.0f, 1.0f,		1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,		 0.0f, 0.0f, 1.0f,		1.0f, 1.0f,
    };

    
    private static final int SIZEOF_MODEL_VERTEX = 6 * Float.BYTES;
    private static final int NORMAL_OFFSET = 3 * Float.BYTES;
    private static final int NUMBER_OF_INSTANCES = 50;
    private static final int TEXCOORD_OFFSET = 6 * Float.BYTES;

    private Camera camera;
    // animation
    private boolean animate = false;
    private float t = 0f;
    // rendering mode
    private int mode = GL_FILL;

    // <editor-fold defaultstate="collapsed" desc="window properties">
    // the window handle
    private long window;
    
    // window size
    private int width;
    private int height;
    private boolean resized = false;
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc="MODELS">
    // models
    private ObjLoader ballerina;
    private ObjLoader scene;
    private ObjLoader lCurtain;
    private ObjLoader rCurtain;
    private ObjLoader seat;
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="TEXTURES">
    private int floorTexture;
    private int curtainTexture;
    private int carpetTexture;
    private int wallTexture;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="TEXTURE LOCATIONS">
    private int textureLoc;
    private int repeatTextureLoc;
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="BUFFERS for models">
    private int ballerinaBuffer;
    private int sceneBuffer;
    private int lCurtainBuffer;
    private int rCurtainBuffer;
    private int seatBuffer;
    private int quadBuffer;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ARRAYS for models">
    private int ballerinaArray;
    private int sceneArray;
    private int lCurtainArray;
    private int rCurtainArray;
    private int seatArray;
    private int quadArray;
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="PROGRAMS"> 
    private int seatProgram;
    private int modelProgram;
    private int texMProgram;
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="seat program uniform locations for SEAT SHADERS">
    private int seatMvpLoc;
    private int seatNLoc;
    private int seatModelLoc;
    private int seatViewLoc;
    private int seatProjectionLoc;
    private int seatLightPositionLoc;
    private int seatLightAmbientColorLoc;
    private int seatLightDiffuseColorLoc;
    private int seatLightSpecularColorLoc;
    private int seatEyePositionLoc;
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="model program uniform locations for MODEL SHADERS">
    private int modelMvpUniformLoc;
    private int modelNUniformLoc;
    private int modelModelLoc;

    private int lightPositionLoc1;
    private int lightAmbientColorLoc1;
    private int lightDiffuseColorLoc1;
    private int lightSpecularColorLoc1;
    
    private int lightPositionLoc2;
    private int lightAmbientColorLoc2;
    private int lightDiffuseColorLoc2;
    private int lightSpecularColorLoc2;

    private int materialAmbientColorLoc;
    private int materialDiffuseColorLoc;
    private int materialSpecularColorLoc;
    private int materialShininessLoc;

    private int eyePositionLoc;
    private int flagLoc;
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="model program uniform locations for TEXM SHADERS">
    private int texMMvpLoc;
    private int texMNLoc;
    private int texMModelLoc;

    private int texMLightPositionLoc;
    private int texMLightAmbientColorLoc;
    private int texMLightDiffuseColorLoc;
    private int texMLightSpecularColorLoc;

    private int texMMaterialAmbientColorLoc;
    private int texMMaterialDiffuseColorLoc;
    private int texMMaterialSpecularColorLoc;
    private int texMMaterialShininessLoc;

    private int texMEyePositionLoc;
    // </editor-fold>
    
    private final Matrix4f[] modelMatrices = new Matrix4f[NUMBER_OF_INSTANCES];
    private final Vector4f[] seatColors = new Vector4f[NUMBER_OF_INSTANCES];
    FloatBuffer seatDataBuffer = BufferUtils.createFloatBuffer(NUMBER_OF_INSTANCES * (16 + 4));

    public static void main(String[] args) {
        new ProjectOpenGL().run();
    }

    public void run() {
        System.out.println("Version of LWJGL is: " + Version.getVersion() + "!");
        camera = new Camera();
        soundThread = new SoundThread(true);
        
        initGLFW();
        threadS = new Thread(soundThread);
        threadS.start();
        loop();
        soundThread.terminate();
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void initGLFW() {
        // Setup an error callback. Print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // set initial width and height
        width = 640;
        height = 480;

        // Create the window
        window = glfwCreateWindow(width, height, "Theatre", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_RELEASE) {
                switch (key) {
                    case GLFW_KEY_ESCAPE:
                        glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
                        break;
                    case GLFW_KEY_A:
                        animate = !animate;
                        break;
                    case GLFW_KEY_T:
                        // TODO toggle fullscreen
                        break;
                    case GLFW_KEY_L:
                        mode = GL_LINE;
                        break;
                    case GLFW_KEY_S:
                        if(soundThread.getRunning().get()){
                            soundThread.terminate();
                            System.err.println("thread was running" + soundThread.getRunning());
                        }else{
                            soundThread.start();
                            System.err.println("START...thread is now "+ soundThread.getRunning() );
                        }
                        break;
                    case GLFW_KEY_F:
                        mode = GL_FILL;
                        break;
                }
            }
        });

        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (action == GLFW_PRESS) {
                if (button == GLFW_MOUSE_BUTTON_1) {
                    camera.updateMouseButton(Camera.Button.LEFT, true);
                } else if (button == GLFW_MOUSE_BUTTON_2) {
                    camera.updateMouseButton(Camera.Button.RIGHT, true);
                }
            } else if (action == GLFW_RELEASE) {
                if (button == GLFW_MOUSE_BUTTON_1) {
                    camera.updateMouseButton(Camera.Button.LEFT, false);
                } else if (button == GLFW_MOUSE_BUTTON_2) {
                    camera.updateMouseButton(Camera.Button.RIGHT, false);
                }
            }
        });

        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            camera.updateMousePosition(xpos, ypos);
        });

        // add window size callback
        glfwSetWindowSizeCallback(window, (window, width, height) -> {
            this.width = width;
            this.height = height;
            resized = true;
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // LWJGL detects the current context in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL bindings available for use.
        GL.createCapabilities();
    }

    private void loop() {
        // Prepare data for rendering
        init();
        
        // Run the rendering loop until pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            render();
            
            glfwSwapBuffers(window); // swap the color buffers
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void init() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glLineWidth(3.0f); // makes lines thicker

        glEnable(GL_DEPTH_TEST);
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        //glEnable(GL_CULL_FACE);
        

        
        // load GLSL program (vertex and fragment shaders)
        try {
            seatProgram = loadProgram("/resources/shaders/seat.vs.glsl",
                    "/resources/shaders/seat.fs.glsl");
            modelProgram = loadProgram("/resources/shaders/model.vs.glsl",
                    "/resources/shaders/model.fs.glsl");
            texMProgram = loadProgram("/resources/shaders/texM.vs.glsl",
                    "/resources/shaders/texM.fs.glsl");
            floorTexture = loadTexture("/resources/textures/carpet3.jpg");
            carpetTexture = loadTexture("/resources/textures/gold.jpg");
            wallTexture = loadTexture("/resources/textures/wall.jpg");
            curtainTexture = loadTexture("/resources/textures/curtains.jpg");
            
        } catch (IOException ex) {
            Logger.getLogger(ProjectOpenGL.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        // <editor-fold defaultstate="collapsed" desc="TexMPROGRAM - GETTING UNIFORM LOC">
        texMMvpLoc = glGetUniformLocation(texMProgram, "MVP");
        texMNLoc = glGetUniformLocation(texMProgram, "N");
        texMModelLoc = glGetUniformLocation(texMProgram, "model");

        texMLightPositionLoc = glGetUniformLocation(texMProgram, "lightPosition");
        texMLightAmbientColorLoc = glGetUniformLocation(texMProgram, "lightAmbientColor");
        texMLightDiffuseColorLoc = glGetUniformLocation(texMProgram, "lightDiffuseColor");
        texMLightSpecularColorLoc = glGetUniformLocation(texMProgram, "lightSpecularColor");

        texMEyePositionLoc = glGetUniformLocation(texMProgram, "eyePosition");

        texMMaterialAmbientColorLoc = glGetUniformLocation(texMProgram, "materialAmbientColor");
        texMMaterialDiffuseColorLoc = glGetUniformLocation(texMProgram, "materialDiffuseColor");
        texMMaterialSpecularColorLoc = glGetUniformLocation(texMProgram, "materialSpecularColor");
        texMMaterialShininessLoc = glGetUniformLocation(texMProgram, "materialShininess");

        repeatTextureLoc = glGetUniformLocation(texMProgram, "repeat");
        textureLoc = glGetUniformLocation(texMProgram, "myTexture");
        
        //</editor-fold>
        
        ////////////////////////////////////////////////////

        // <editor-fold defaultstate="collapsed" desc="ModelPROGRAM - GETTING UNIFORM LOC">
        // model program uniforms
        modelMvpUniformLoc = glGetUniformLocation(modelProgram, "MVP");
        modelNUniformLoc = glGetUniformLocation(modelProgram, "N");
        modelModelLoc = glGetUniformLocation(modelProgram, "model");

        //storing uniform variable locations LIGHT
        lightPositionLoc1 = glGetUniformLocation(modelProgram, "lightPosition1");
        lightAmbientColorLoc1 = glGetUniformLocation(modelProgram, "lightAmbientColor1");
        lightDiffuseColorLoc1 = glGetUniformLocation(modelProgram, "lightDiffuseColor1");
        lightSpecularColorLoc1 = glGetUniformLocation(modelProgram, "lightSpecularColor1");
        
        //storing uniform variable locations for LIGHT2
        lightPositionLoc2 = glGetUniformLocation(modelProgram, "lightPosition2");
        lightAmbientColorLoc2 = glGetUniformLocation(modelProgram, "lightAmbientColor2");
        lightDiffuseColorLoc2 = glGetUniformLocation(modelProgram, "lightDiffuseColor2");
        lightSpecularColorLoc2 = glGetUniformLocation(modelProgram, "lightSpecularColor2");

        //storing uniform variable locations for eyePosition
        eyePositionLoc = glGetUniformLocation(modelProgram, "eyePosition");

        //storing uniform variable locations for material properties
        materialAmbientColorLoc = glGetUniformLocation(modelProgram, "materialAmbientColor");
        materialDiffuseColorLoc = glGetUniformLocation(modelProgram, "materialDiffuseColor");
        materialSpecularColorLoc = glGetUniformLocation(modelProgram, "materialSpecularColor");
        materialShininessLoc = glGetUniformLocation(modelProgram, "materialShininess");
        flagLoc = glGetUniformLocation(modelProgram, "flag");
        //</editor-fold>
        
        //////////////////////////////////////////////////////////////////////////
        
        // <editor-fold defaultstate="collapsed" desc="SeatPROGRAM - GETTING UNIFORM LOC">
        //SEATS
        seatMvpLoc = glGetUniformLocation(seatProgram, "MVP");
        seatNLoc = glGetUniformLocation(seatProgram, "N");
        seatModelLoc = glGetUniformLocation(seatProgram, "model");
        
        seatProjectionLoc = glGetUniformLocation(seatProgram, "projection");
        seatViewLoc = glGetUniformLocation(seatProgram, "view");

        seatLightPositionLoc = glGetUniformLocation(seatProgram, "lightPosition");
        seatLightAmbientColorLoc = glGetUniformLocation(seatProgram, "lightAmbientColor");
        seatLightDiffuseColorLoc = glGetUniformLocation(seatProgram, "lightDiffuseColor");
        seatLightSpecularColorLoc = glGetUniformLocation(seatProgram, "lightSpecularColor");
        
        seatEyePositionLoc = glGetUniformLocation(seatProgram, "eyePosition");
        //</editor-fold>

        
        for(int i=0; i< NUMBER_OF_INSTANCES; i++){
            if(i % 8 >=4){
                modelMatrices[i] = new Matrix4f().translate((i % 8 - 4.8f) * 6f, -14.25f, (i/10 +7f) * 6f)
                        .rotate(110, 0f, 1f, 0f)
                        .scale(0.05f);
            }else{
                modelMatrices[i] = new Matrix4f().translate((i % 8 - 6.2f) * 6f, -14.25f, (i/10 +7f) * 6f)
                        .rotate(110, 0f, 1f, 0f)
                        .scale(0.05f);
            }
        }
       
        for(int i=0; i< NUMBER_OF_INSTANCES; i++){
            seatColors[i] = new Vector4f(0.126f, 0.150f, 0.200f, 1f);
            
        }
        
        // create buffers with geometry
        int[] buffers = new int[6];
        glGenBuffers(buffers);
        sceneBuffer = buffers[0];
        ballerinaBuffer = buffers[1];
        seatBuffer = buffers[2];
        lCurtainBuffer = buffers[3];
        rCurtainBuffer = buffers[4];
        quadBuffer = buffers[5];

        // load and fill object data
        ballerina = new ObjLoader("/resources/models/ballerina.obj");
        scene = new ObjLoader("/resources/models/scene.obj");
        seat = new ObjLoader("/resources/models/seat1.obj");
        lCurtain = new ObjLoader("/resources/models/leftCurtain.obj");
        rCurtain = new ObjLoader("/resources/models/rightCurtain.obj");
        try {
            ballerina.load();
            scene.load();
            seat.load();
            lCurtain.load();
            rCurtain.load();
        } catch (IOException ex) {
            Logger.getLogger(ProjectOpenGL.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        
        glBindBuffer(GL_ARRAY_BUFFER, quadBuffer);
        glBufferData(GL_ARRAY_BUFFER, QUAD, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        // <editor-fold defaultstate="collapsed" desc="LEFT CURTAIN Binding data to buffer">
        int length = 3 * 8 * lCurtain.getTriangleCount();
        FloatBuffer lCurtainData = BufferUtils.createFloatBuffer(length);
        for (int f = 0; f < this.lCurtain.getTriangleCount(); f++) {
            int[] pi = this.lCurtain.getVertexIndices().get(f);
            int[] ni = this.lCurtain.getNormalIndices().get(f);
            int[] ti = this.lCurtain.getTexcoordIndices().get(f);
            for (int i = 0; i < 3; i++) {
                float[] position = this.lCurtain.getVertices().get(pi[i]);
                float[] normal = this.lCurtain.getNormals().get(ni[i]);
                float[] texCoord = this.lCurtain.getTexcoords().get(ti[i]);
                lCurtainData.put(position);
                lCurtainData.put(normal);
                lCurtainData.put(texCoord);
            }
        }
        lCurtainData.rewind();
        glBindBuffer(GL_ARRAY_BUFFER, lCurtainBuffer);
        glBufferData(GL_ARRAY_BUFFER, lCurtainData, GL_STATIC_DRAW);
        // clear buffer binding
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        //</editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc="RIGHT CURTAIN Binding data to buffer">
        length = 3 * 8 * rCurtain.getTriangleCount();
        FloatBuffer rCurtainData = BufferUtils.createFloatBuffer(length);
        for (int f = 0; f < this.rCurtain.getTriangleCount(); f++) {
            int[] pi = this.rCurtain.getVertexIndices().get(f);
            int[] ni = this.rCurtain.getNormalIndices().get(f);
            int[] ti = this.rCurtain.getTexcoordIndices().get(f);
            for (int i = 0; i < 3; i++) {
                float[] position = this.rCurtain.getVertices().get(pi[i]);
                float[] normal = this.rCurtain.getNormals().get(ni[i]);
                float[] texCoord = this.rCurtain.getTexcoords().get(ti[i]);
                rCurtainData.put(position);
                rCurtainData.put(normal);
                rCurtainData.put(texCoord);
            }
        }
        rCurtainData.rewind();
        glBindBuffer(GL_ARRAY_BUFFER, rCurtainBuffer);
        glBufferData(GL_ARRAY_BUFFER, rCurtainData, GL_STATIC_DRAW);
        // clear buffer binding
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc="BALLERINA Binding data to buffer">
        length = 3 * 6 * ballerina.getTriangleCount();
        FloatBuffer ballerinaData = BufferUtils.createFloatBuffer(length);
        for (int f = 0; f < this.ballerina.getTriangleCount(); f++) {
            int[] pi = this.ballerina.getVertexIndices().get(f);
            int[] ni = this.ballerina.getNormalIndices().get(f);
            for (int i = 0; i < 3; i++) {
                float[] position = this.ballerina.getVertices().get(pi[i]);
                float[] normal = this.ballerina.getNormals().get(ni[i]);
                ballerinaData.put(position);
                ballerinaData.put(normal);
            }
        }
        ballerinaData.rewind();
        glBindBuffer(GL_ARRAY_BUFFER, ballerinaBuffer);
        glBufferData(GL_ARRAY_BUFFER, ballerinaData, GL_STATIC_DRAW);
        // clear buffer binding
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc="SCENE Binding data to buffer">
        length = 3 * 8 * scene.getTriangleCount();
        FloatBuffer sceneData = BufferUtils.createFloatBuffer(length);
        for (int f = 0; f < this.scene.getTriangleCount(); f++) {
            int[] pi = this.scene.getVertexIndices().get(f);
            int[] ni = this.scene.getNormalIndices().get(f);
            int[] ti = this.scene.getTexcoordIndices().get(f);
            for (int i = 0; i < 3; i++) {
                float[] position = this.scene.getVertices().get(pi[i]);
                float[] normal = this.scene.getNormals().get(ni[i]);
                float[] texcoord = this.scene.getTexcoords().get(ni[i]);
                sceneData.put(position);
                sceneData.put(normal);
                sceneData.put(texcoord);
            }
        }
        sceneData.rewind();
        glBindBuffer(GL_ARRAY_BUFFER, sceneBuffer);
        glBufferData(GL_ARRAY_BUFFER, sceneData, GL_STATIC_DRAW);
        // clear buffer binding
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc="SEAT Binding data to buffer">
        int seatLength = 3 * 8 * seat.getTriangleCount();
        FloatBuffer seatData = BufferUtils.createFloatBuffer(seatLength);
        for (int f = 0; f < seat.getTriangleCount(); f++) {
            int[] pi = seat.getVertexIndices().get(f);
            int[] ni = seat.getNormalIndices().get(f);
            for (int i = 0; i < 3; i++) {
                float[] position = seat.getVertices().get(pi[i]);
                float[] normal = seat.getNormals().get(ni[i]);
                seatData.put(position);
                seatData.put(normal);
            }
        }
        seatData.rewind();
        glBindBuffer(GL_ARRAY_BUFFER, seatBuffer);
        glBufferData(GL_ARRAY_BUFFER, seatData, GL_STATIC_DRAW);
        // clear buffer binding
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        // </editor-fold>

        // create a vertex array object for the geometry
        int[] arrays = new int[6];
        glGenVertexArrays(arrays);
        sceneArray = arrays[0];
        ballerinaArray = arrays[1];
        seatArray = arrays[2];
        lCurtainArray = arrays[3];
        rCurtainArray = arrays[4];
        quadArray = arrays[5];


        // get model program attributes
        int positionAttribLoc = glGetAttribLocation(modelProgram, "position");
        int normalAttribLoc = glGetAttribLocation(modelProgram, "normal");

        // bind ballerinaData buffer
        glBindVertexArray(ballerinaArray);
        glBindBuffer(GL_ARRAY_BUFFER, ballerinaBuffer);
        glEnableVertexAttribArray(positionAttribLoc);
        glVertexAttribPointer(positionAttribLoc, 3, GL_FLOAT, false, SIZEOF_MODEL_VERTEX, 0);
        glEnableVertexAttribArray(normalAttribLoc);
        glVertexAttribPointer(normalAttribLoc, 3, GL_FLOAT, false, SIZEOF_MODEL_VERTEX, NORMAL_OFFSET);
        
        // bind sceneData buffer
        glBindVertexArray(sceneArray);
        glBindBuffer(GL_ARRAY_BUFFER, sceneBuffer);
        glEnableVertexAttribArray(positionAttribLoc);
        glVertexAttribPointer(positionAttribLoc, 3, GL_FLOAT, false,8*Float.BYTES , 0);
        glEnableVertexAttribArray(normalAttribLoc);
        glVertexAttribPointer(normalAttribLoc, 3, GL_FLOAT, false, 8*Float.BYTES, NORMAL_OFFSET);
        
        
        positionAttribLoc = glGetAttribLocation(texMProgram, "position");
        normalAttribLoc = glGetAttribLocation(texMProgram, "normal");
        int texAttribLoc = glGetAttribLocation(texMProgram, "texcoord");
        
        // bind leftCurtain buffer
        glBindVertexArray(lCurtainArray);
        glBindBuffer(GL_ARRAY_BUFFER, lCurtainBuffer);
        glEnableVertexAttribArray(positionAttribLoc);
        glVertexAttribPointer(positionAttribLoc, 3, GL_FLOAT, false,  8*Float.BYTES, 0);
        glEnableVertexAttribArray(normalAttribLoc);
        glVertexAttribPointer(normalAttribLoc, 3, GL_FLOAT, false,  8*Float.BYTES, NORMAL_OFFSET);
        glEnableVertexAttribArray(texAttribLoc);
        glVertexAttribPointer(texAttribLoc, 2, GL_FLOAT, false, 8*Float.BYTES, 6*Float.BYTES);
        
        // bind rightCurtain buffer
        glBindVertexArray(rCurtainArray);
        glBindBuffer(GL_ARRAY_BUFFER, rCurtainBuffer);
        glEnableVertexAttribArray(positionAttribLoc);
        glVertexAttribPointer(positionAttribLoc, 3, GL_FLOAT, false,  8*Float.BYTES, 0);
        glEnableVertexAttribArray(normalAttribLoc);
        glVertexAttribPointer(normalAttribLoc, 3, GL_FLOAT, false,  8*Float.BYTES, NORMAL_OFFSET);
        glEnableVertexAttribArray(texAttribLoc);
        glVertexAttribPointer(texAttribLoc, 2, GL_FLOAT, false, 8*Float.BYTES, 6*Float.BYTES);
        
        
        glBindVertexArray(quadArray);
        glBindBuffer(GL_ARRAY_BUFFER, quadBuffer);
        glEnableVertexAttribArray(positionAttribLoc);
        glVertexAttribPointer(positionAttribLoc, 3, GL_FLOAT, false,  8*Float.BYTES, 0);
        glEnableVertexAttribArray(normalAttribLoc);
        glVertexAttribPointer(normalAttribLoc, 3, GL_FLOAT, false,  8*Float.BYTES, NORMAL_OFFSET);
        glEnableVertexAttribArray(texAttribLoc);
        glVertexAttribPointer(texAttribLoc, 2, GL_FLOAT, false, 8*Float.BYTES, 6*Float.BYTES);
        
        
        positionAttribLoc = glGetAttribLocation(seatProgram, "position");
        normalAttribLoc = glGetAttribLocation(seatProgram, "normal");
        
        // bind seat buffer
        glBindVertexArray(seatArray);
        glBindBuffer(GL_ARRAY_BUFFER, seatBuffer);
        glEnableVertexAttribArray(positionAttribLoc);
        glVertexAttribPointer(positionAttribLoc, 3, GL_FLOAT, false, SIZEOF_MODEL_VERTEX, 0);
        glEnableVertexAttribArray(normalAttribLoc);
        glVertexAttribPointer(normalAttribLoc, 3, GL_FLOAT, false, SIZEOF_MODEL_VERTEX, NORMAL_OFFSET);

        // clear bindings, so that other code doesn't presume it (easier error detection)
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void render() {
        // Resize OpenGL viewport, i.e., the (bitmap) extents to that is the
        // OpenGL screen space [-1, 1] mapped.
        if (resized) {
            glViewport(0, 0, width, height);
            resized = false;
        }

        // animate variables
        if (animate) {
            t += 0.002f;
        }

        glPolygonMode(GL_FRONT_AND_BACK, mode);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Matrix4f projection = new Matrix4f()
                .perspective((float) Math.toRadians(60f), width / (float) height, 1, 500);

        Matrix4f view = new Matrix4f()
                .lookAt(camera.getEyePosition(), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));

        
        
        
        
        //drawing curtains 
        Material matCurtain = new Material(new Vector3f(0.33f, 0.22f, 0.03f), new Vector3f(0.4f, 0.11f, 0.1f), new Vector3f(0.4f, 0.2f, 0.2f), 27.90f);      
        if(t <1f){
             drawModelWithTex(new Matrix4f().translate(0, 15, 0).scale(6f).scale(1f, -1f + t , 1f), view, projection, lCurtainArray, 0, lCurtain.getTriangleCount() * 3, null, curtainTexture);
        }
       
        if(t <1f){
             drawModelWithTex(new Matrix4f().translate(0, 15, 0).scale(6f).scale(1f, -1f + t , 1f), view, projection, rCurtainArray, 0, rCurtain.getTriangleCount() * 3, matCurtain, curtainTexture);
        }
        
        //SmallCARPET
       drawModelWithTex(new Matrix4f().translate(0, -14.90f, 32).rotate((float)Math.toRadians(90), 1f, 0f, 0f)
               .scale(5f, 16f, 1f), view, projection, quadArray, 0, 6, null, carpetTexture);
        
        //Main CARPET
        drawModelWithTex(new Matrix4f().translate(0, -15, 18).rotate((float)Math.toRadians(90), 1f, 0f, 0f).scale(30f, 30f, 1f), view, projection, quadArray, 0, 6, null, floorTexture);
        
        //LEFT wall
        drawModelWithTex(new Matrix4f().translate(-30, 10, 18).rotate((float)Math.toRadians(90), 0f, 1f, 0f).scale(30f, 25f, 1f), view, projection, quadArray, 0, 36, null, floorTexture);
        
        //RIGHT wall
        drawModelWithTex(new Matrix4f().translate(30, 10, 18).rotate((float)Math.toRadians(90), 0f, 1f, 0f).scale(30f, 25f, 1f), view, projection, quadArray, 0, 36, null, floorTexture);
        
        //behind = rear wall
        drawModelWithTex(new Matrix4f().translate(0, 10, -12).scale(30f, 25f, 1f), view, projection, quadArray, 0, 36, null, floorTexture);
        
        //ceil
        drawModelWithTex(new Matrix4f().translate(0, 35, 18).rotate((float)Math.toRadians(90), 1f, 0f, 0f).scale(30f, 30f, 1f), view, projection, quadArray, 0, 6, null, floorTexture);
        
        
        
        //drawing SCENE 
        Material matScene = new Material(new Vector3f(0.25f), new Vector3f(0.15f), new Vector3f(0.26f, 0.14f, 0.09f), 12.8f);
        drawModel(new Matrix4f().translate(0, -15, 0).scale(6f), view, projection, sceneArray, scene.getTriangleCount() * 3, matScene,0);
        // drawing seats
        drawSeats(new Matrix4f(), view, projection, seatArray, seat.getTriangleCount() * 3);

        //drawing ballerinas
        Material matBalCenter = new Material(new Vector3f(0.33f, 0.22f, 0.03f), new Vector3f(0.78f, 0.57f, 0.11f), new Vector3f(0.99f, 0.94f, 0.81f), 27.90f);
        drawModel(new Matrix4f().translate(0, -5.25f, -5).rotate(6*t, 0f, 1f, 0f), view.rotate(6*t, 0f, 1f, 0f), projection, ballerinaArray, ballerina.getTriangleCount() * 3, matBalCenter,0);

        Material matBalLeft = new Material(new Vector3f(0.21f, 0.13f, 0.05f), new Vector3f(0.71f, 0.43f, 0.18f), new Vector3f(0.39f, 0.27f, 0.17f), 25.6f);
        drawModel(new Matrix4f().translate(-5, -5.25f, 0).rotate(-30, 0f, 1f, 0f).rotate(6*t, 0f, 1f, 0f), view, projection, ballerinaArray, ballerina.getTriangleCount() * 3, matBalLeft, 0);

        Material matBalRight = new Material(new Vector3f(0.25f), new Vector3f(0.4f), new Vector3f(0.26f, 0.14f, 0.09f), 12.8f);
        drawModel(new Matrix4f().translate(5, -5.25f, 0).rotate(30, 0f, 1f, 0f).rotate(6*t, 0f, 1f, 0f), view, projection, ballerinaArray, ballerina.getTriangleCount() * 3, matBalRight, 0);
        
        
        
    }
    
    private void drawSeats(Matrix4f model, Matrix4f view, Matrix4f projection, int vao, int count) {
        glUseProgram(seatProgram);
        glBindVertexArray(vao);

        glUniform3f(seatEyePositionLoc, camera.getEyePosition().x, camera.getEyePosition().y, camera.getEyePosition().z);

        FloatBuffer projectionData = BufferUtils.createFloatBuffer(16);
        FloatBuffer viewData = BufferUtils.createFloatBuffer(16);
        
        projection.get(projectionData);
        view.get(viewData);
        
        glUniformMatrix4fv(seatProjectionLoc, false, projectionData);
        glUniformMatrix4fv(seatViewLoc, false, viewData);
        
        FloatBuffer modelData = BufferUtils.createFloatBuffer(16);
        for(int i=0; i< NUMBER_OF_INSTANCES; i++){
            modelMatrices[i].get(modelData);
            int modelLoc = glGetUniformLocation(seatProgram, "model[" + i + "]");
            glUniformMatrix4fv(modelLoc, false, modelData); 
        }
        
        FloatBuffer colorData = BufferUtils.createFloatBuffer(4);
        for(int i=0; i< NUMBER_OF_INSTANCES; i++){
            seatColors[i].get(colorData);
            int colorLoc = glGetUniformLocation(seatProgram, "color[" + i + "]");
            glUniform4fv(colorLoc, colorData); 
        }
        
        //glUniformMatrix4fv(seatNLoc, false, modelData);      
//        
        glUniform4f(seatLightPositionLoc, 2, 5, 3, 1);
        glUniform3f(seatLightAmbientColorLoc, 0.3f, 0.3f, 0.3f);
        glUniform3f(seatLightDiffuseColorLoc, 1, 1, 1);
        glUniform3f(seatLightSpecularColorLoc, 1, 1, 1);

        glDrawArraysInstanced(GL_TRIANGLES, 0, count, NUMBER_OF_INSTANCES);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindVertexArray(0);
        glUseProgram(0);
    }
    
    private Vector4f randomColor() {
        Random random = new Random();

        float hue = random.nextFloat();
        float saturation = random.nextFloat();
        float brightness = 0.7f;

        Color color = Color.getHSBColor(hue, saturation, brightness);

        return new Vector4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1);
    }
    
    
    private void drawModelWithTex(Matrix4f model, Matrix4f view, Matrix4f projection, 
            int vao, int offset, int count, Material material, int texture) {
        // compute model-view-projection matrix
        Matrix4f mvp = new Matrix4f(projection)
                .mul(view)
                .mul(model);

        // compute normal matrix
        Matrix3f n = model.get3x3(new Matrix3f())
                .invert()
                .transpose();

        glUseProgram(texMProgram);
        glBindVertexArray(vao); // bind vertex array to draw
        
        glUniform1f(repeatTextureLoc, 10);

        glUniform4f(texMLightPositionLoc, 0f, 5f, -5f, 1);
        glUniform3f(texMLightAmbientColorLoc, 0.3f, 0.3f, 0.3f);
        glUniform3f(texMLightDiffuseColorLoc, 1, 1, 1);
        glUniform3f(texMLightSpecularColorLoc, 0.6f, 0.6f, 0.6f);

        glUniform3f(texMEyePositionLoc, camera.getEyePosition().x, camera.getEyePosition().y, camera.getEyePosition().z);

        if (material != null) {
            glUniform3f(texMMaterialAmbientColorLoc, material.getAmbientColor().x, material.getAmbientColor().y, material.getAmbientColor().z);
            glUniform3f(texMMaterialDiffuseColorLoc, material.getDiffuseColor().x, material.getDiffuseColor().y, material.getDiffuseColor().z);
            glUniform3f(texMMaterialSpecularColorLoc, material.getSpecularColor().x, material.getSpecularColor().y, material.getSpecularColor().z);
            glUniform1f(texMMaterialShininessLoc, material.getShininess());
        }

        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glUniform1i(textureLoc, 0);
        /*
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, rocksTexture);
        glUniform1i(rocksTexLoc, 2);*/

        FloatBuffer mvpData = BufferUtils.createFloatBuffer(16);
        FloatBuffer nData = BufferUtils.createFloatBuffer(9);
        FloatBuffer modelData = BufferUtils.createFloatBuffer(16);
        mvp.get(mvpData);
        n.get(nData);
        model.get(modelData);
        glUniformMatrix4fv(texMMvpLoc, false, mvpData); // pass MVP matrix to shader
        glUniformMatrix3fv(texMNLoc, false, nData); // pass Normal matrix to shader
        glUniformMatrix4fv(texMModelLoc, false, modelData); // pass model matrix to shader

        glDrawArrays(GL_TRIANGLES, offset, count);

        glBindTexture(GL_TEXTURE_2D, 0);
        glBindVertexArray(0);
        glUseProgram(0);
    }
    

    private void drawModel(Matrix4f model, Matrix4f view, Matrix4f projection, int vao, int count, Material material, int flag) {
        // compute model-view-projection matrix
        Matrix4f mvp = new Matrix4f(projection)
                .mul(view)
                .mul(model);

        // compute normal matrix
        Matrix3f n = model.get3x3(new Matrix3f())
                .invert()
                .transpose();

        glUseProgram(modelProgram);
        glBindVertexArray(vao); // bind vertex array to draw

        glUniform1i(flagLoc, flag);
        //setting values for light 
        //light as a point light
        glUniform4f(lightPositionLoc1, 0, 40, 0, 1);
        glUniform3f(lightAmbientColorLoc1, 0.5f, 0.5f, 0.5f);
        glUniform3f(lightDiffuseColorLoc1, 1, 1, 1);
        glUniform3f(lightSpecularColorLoc1, 1, 1, 1);
        
        //second light
        glUniform4f(lightPositionLoc2, 0, 5, -2, 0);
        glUniform3f(lightAmbientColorLoc2, 0.45f, 0.45f, 0.45f);
        glUniform3f(lightDiffuseColorLoc2, 1, 1, 1);
        glUniform3f(lightSpecularColorLoc2, 1, 1, 1);

        //eye position as camera position
        glUniform3f(eyePositionLoc, camera.getEyePosition().x, camera.getEyePosition().y, camera.getEyePosition().z);

        //setting values for material
        glUniform3f(materialAmbientColorLoc, material.getAmbientColor().x, material.getAmbientColor().y, material.getAmbientColor().z);
        glUniform3f(materialDiffuseColorLoc, material.getDiffuseColor().x, material.getDiffuseColor().y, material.getDiffuseColor().z);
        glUniform3f(materialSpecularColorLoc, material.getSpecularColor().x, material.getSpecularColor().y, material.getSpecularColor().z);
        glUniform1f(materialShininessLoc, material.getShininess());

        FloatBuffer mvpData = BufferUtils.createFloatBuffer(16);
        FloatBuffer nData = BufferUtils.createFloatBuffer(9);
        FloatBuffer modelData = BufferUtils.createFloatBuffer(16);
        mvp.get(mvpData);
        n.get(nData);
        model.get(modelData);
        glUniformMatrix4fv(modelMvpUniformLoc, false, mvpData); // pass MVP matrix to shader
        glUniformMatrix3fv(modelNUniformLoc, false, nData); // pass Normal matrix to shader
        glUniformMatrix4fv(modelModelLoc, false, modelData); // pass model matrix to shader

        glDrawArrays(GL_TRIANGLES, 0, count);

        glBindVertexArray(0);
        glUseProgram(0);
    }
    
    private int loadShader(String filename, int shaderType) throws IOException {
        String source = readAllFromResource(filename);
        int shader = glCreateShader(shaderType);

        // create and compile GLSL shader
        glShaderSource(shader, source);
        glCompileShader(shader);

        // check GLSL shader compile status
        int status = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (status == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            System.err.println(log);
        }

        return shader;
    }

    private int loadProgram(String vertexShaderFile, String fragmentShaderFile) throws IOException {
        // load vertex and fragment shaders (GLSL)
        int vs = loadShader(vertexShaderFile, GL_VERTEX_SHADER);
        int fs = loadShader(fragmentShaderFile, GL_FRAGMENT_SHADER);

        // create GLSL program, attach shaders and compile it
        int program = glCreateProgram();
        glAttachShader(program, vs);
        glAttachShader(program, fs);
        glLinkProgram(program);

        int status = glGetProgrami(program, GL_LINK_STATUS);
        if (status == GL_FALSE) {
            String log = glGetProgramInfoLog(program);
            System.err.println(log);
        }

        return program;
    }

    private String readAllFromResource(String resource) throws IOException {
        InputStream is = ProjectOpenGL.class.getResourceAsStream(resource);
        if (is == null) {
            throw new IOException("Resource not found: " + resource);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        int c;
        while ((c = reader.read()) != -1) {
            sb.append((char) c);
        }

        return sb.toString();
    }

    private int loadTexture(String filename) throws IOException {
        BufferedImage image = ImageIO.read(ProjectOpenGL.class.getResourceAsStream(filename));
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        int internalFormat;
        int format;
        switch (image.getType()) {
            case BufferedImage.TYPE_3BYTE_BGR:
                internalFormat = GL_RGB;
                format = GL_BGR;
                break;
            case BufferedImage.TYPE_4BYTE_ABGR:
                internalFormat = GL_RGBA;
                format = GL_BGRA;
                pixels = toBGRA(pixels);
                break;
            default:
                throw new IOException("Unknown image type: " + image.getType());
        }

        ByteBuffer textureData = BufferUtils.createByteBuffer(pixels.length);
        textureData.put(pixels);
        textureData.rewind();

        int texture;
        // Task 1:  create GL texture object using glGenTextures() and store it in texture local variable
        //          bind the texture using glBindTexture(GL_TEXTURE_2D, texture)
        //          upload texture data using glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, <width>, <height>, 0, format, GL_UNSIGNED_BYTE, <data>)
        //              get width and height from image object
        //              data is loaded to ByteBuffer textureData
        // Task 3:  generate mipmap levels using glGenerateMipmap(GL_TEXTURE_2D)
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, image.getWidth(), image.getHeight(), 0, format, GL_UNSIGNED_BYTE, textureData);
        glGenerateMipmap(GL_TEXTURE_2D);

        // Task 1:  set texture filtering to GL_NEAREST... 
        //minification: glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        //magnification: glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        // Task 2:  set texture filtering using glTexParameteri(...) to GL_LINEAR
        //              minification filter: glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        //              magnification filter: glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        // Task 3:  change minification filter (GL_TEXTURE_MIN_FILTER) from GL_LINEAR to GL_LINEAR_MIPMAP_LINEAR
        // Task 5:  set texture wrap mode to GL_MIRRORED_REPEAT in both S and T directions
        //              S direction: glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT)
        //              T direction: glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT)
        //              also try other modes, listed in the attached PDF :)
        
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);        
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);

        // unbind texture
        glBindTexture(GL_TEXTURE_2D, 0);

        return texture;
    }

    private byte[] toBGRA(byte[] abgr) {
        byte[] bgra = new byte[abgr.length];
        for (int i = 0; i < abgr.length; i += 4) {
            bgra[i] = abgr[i + 1];
            bgra[i + 1] = abgr[i + 2];
            bgra[i + 2] = abgr[i + 3];
            bgra[i + 3] = abgr[i];
        }
        return bgra;
    }
    
}
