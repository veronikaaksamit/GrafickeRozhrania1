package cz.muni.fi.pv112.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class ProjectOpenGL {
    private static final int SIZEOF_MODEL_VERTEX = 6 * Float.BYTES;
    private static final int NORMAL_OFFSET = 3 * Float.BYTES;

    private Camera camera;

    // the window handle
    private long window;

    // window size
    private int width;
    private int height;
    private boolean resized = false;

    // animation
    private boolean animate = false;
    private float t = 0f;

    // rendering mode
    private int mode = GL_FILL;

    // models
    private ObjLoader ballerina;
    private ObjLoader scene;
    private ObjLoader seat;

    // Buffers and arrays for models
    private int ballerinaBuffer;
    private int sceneBuffer;
    private int seatBuffer;
    
    private int ballerinaArray;
    private int sceneArray;
    private int seatArray;
    

    private int modelProgram;
    private int modelMvpUniformLoc;
    private int modelNUniformLoc;
    private int modelModelLoc;

    private int lightPositionLoc;
    private int lightAmbientColorLoc;
    private int lightDiffuseColorLoc;
    private int lightSpecularColorLoc;

    private int materialAmbientColorLoc;
    private int materialDiffuseColorLoc;
    private int materialSpecularColorLoc;
    private int materialShininessLoc;

    private int eyePositionLoc;

    public static void main(String[] args) {
        new ProjectOpenGL().run();
    }

    public void run() {
        System.out.println("Version of LWJGL is: " + Version.getVersion() + "!");
        camera = new Camera();
        initGLFW();
        loop();
        
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
        // default
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glLineWidth(3.0f); // makes lines thicker

        glEnable(GL_DEPTH_TEST);

        // load GLSL program (vertex and fragment shaders)
        try {
            modelProgram = loadProgram("/resources/shaders/model.vs.glsl",
                    "/resources/shaders/model.fs.glsl");
        } catch (IOException ex) {
            Logger.getLogger(ProjectOpenGL.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        // model program uniforms
        modelMvpUniformLoc = glGetUniformLocation(modelProgram, "MVP");
        modelNUniformLoc = glGetUniformLocation(modelProgram, "N");
        modelModelLoc = glGetUniformLocation(modelProgram, "model");

        //storing uniform variable locations LIGHT
        lightPositionLoc = glGetUniformLocation(modelProgram, "lightPosition");
        lightAmbientColorLoc = glGetUniformLocation(modelProgram, "lightAmbientColor");
        lightDiffuseColorLoc = glGetUniformLocation(modelProgram, "lightDiffuseColor");
        lightSpecularColorLoc = glGetUniformLocation(modelProgram, "lightSpecularColor");

        //storing uniform variable locations for eyePosition
        eyePositionLoc = glGetUniformLocation(modelProgram, "eyePosition");

        //storing uniform variable locations for material properties
        materialAmbientColorLoc = glGetUniformLocation(modelProgram, "materialAmbientColor");
        materialDiffuseColorLoc = glGetUniformLocation(modelProgram, "materialDiffuseColor");
        materialSpecularColorLoc = glGetUniformLocation(modelProgram, "materialSpecularColor");
        materialShininessLoc = glGetUniformLocation(modelProgram, "materialShininess");

        // create buffers with geometry
        int[] buffers = new int[3];
        glGenBuffers(buffers);
        sceneBuffer = buffers[0];
        ballerinaBuffer = buffers[1];
        seatBuffer = buffers[2];


        // load ballerina and fill buffer with ballerina data
        ballerina = new ObjLoader("/resources/models/ballerina.obj");
        scene = new ObjLoader("/resources/models/scene.obj");
        seat = new ObjLoader("/resources/models/UV_modely/box.obj");
        try {
            ballerina.load();
            scene.load();
            seat.load();
        } catch (IOException ex) {
            Logger.getLogger(ProjectOpenGL.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        int length = 3 * 6 * ballerina.getTriangleCount();
        FloatBuffer ballerina = BufferUtils.createFloatBuffer(length);
        for (int f = 0; f < this.ballerina.getTriangleCount(); f++) {
            int[] pi = this.ballerina.getVertexIndices().get(f);
            int[] ni = this.ballerina.getNormalIndices().get(f);
            for (int i = 0; i < 3; i++) {
                float[] position = this.ballerina.getVertices().get(pi[i]);
                float[] normal = this.ballerina.getNormals().get(ni[i]);
                ballerina.put(position);
                ballerina.put(normal);
            }
        }
        ballerina.rewind();
        glBindBuffer(GL_ARRAY_BUFFER, ballerinaBuffer);
        glBufferData(GL_ARRAY_BUFFER, ballerina, GL_STATIC_DRAW);

        // clear buffer binding, so that other code doesn't presume it (easier error detection)
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        
        length = 3 * 6 * scene.getTriangleCount();
        FloatBuffer scene = BufferUtils.createFloatBuffer(length);
        for (int f = 0; f < this.scene.getTriangleCount(); f++) {
            int[] pi = this.scene.getVertexIndices().get(f);
            int[] ni = this.scene.getNormalIndices().get(f);
            for (int i = 0; i < 3; i++) {
                float[] position = this.scene.getVertices().get(pi[i]);
                float[] normal = this.scene.getNormals().get(ni[i]);
                scene.put(position);
                scene.put(normal);
            }
        }
        scene.rewind();
        glBindBuffer(GL_ARRAY_BUFFER, sceneBuffer);
        glBufferData(GL_ARRAY_BUFFER, scene, GL_STATIC_DRAW);
        // clear buffer binding
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        length = 3 * 6 * seat.getTriangleCount();
        FloatBuffer seat = BufferUtils.createFloatBuffer(length);
        for (int f = 0; f < this.seat.getTriangleCount(); f++) {
            int[] pi = this.seat.getVertexIndices().get(f);
            int[] ni = this.seat.getNormalIndices().get(f);
            for (int i = 0; i < 3; i++) {
                float[] position = this.seat.getVertices().get(pi[i]);
                float[] normal = this.seat.getNormals().get(ni[i]);
                seat.put(position);
                seat.put(normal);
            }
        }
        seat.rewind();
        glBindBuffer(GL_ARRAY_BUFFER, seatBuffer);
        glBufferData(GL_ARRAY_BUFFER, seat, GL_STATIC_DRAW);
        // clear buffer binding
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // create a vertex array object for the geometry
        int[] arrays = new int[3];
        glGenVertexArrays(arrays);
        sceneArray = arrays[0];
        ballerinaArray = arrays[1];
        seatArray = arrays[2];

        int positionAttribLoc;
        int colorAttribLoc;

        // get cube program attributes
        positionAttribLoc = glGetAttribLocation(modelProgram, "position");
        int normalAttribLoc = glGetAttribLocation(modelProgram, "normal");

        // bind ballerina buffer
        glBindVertexArray(ballerinaArray);
        glBindBuffer(GL_ARRAY_BUFFER, ballerinaBuffer);
        glEnableVertexAttribArray(positionAttribLoc);
        glVertexAttribPointer(positionAttribLoc, 3, GL_FLOAT, false, SIZEOF_MODEL_VERTEX, 0);
        glEnableVertexAttribArray(normalAttribLoc);
        glVertexAttribPointer(normalAttribLoc, 3, GL_FLOAT, false, SIZEOF_MODEL_VERTEX, NORMAL_OFFSET);
        
        // bind scene buffer
        glBindVertexArray(sceneArray);
        glBindBuffer(GL_ARRAY_BUFFER, sceneBuffer);
        glEnableVertexAttribArray(positionAttribLoc);
        glVertexAttribPointer(positionAttribLoc, 3, GL_FLOAT, false, SIZEOF_MODEL_VERTEX, 0);
        glEnableVertexAttribArray(normalAttribLoc);
        glVertexAttribPointer(normalAttribLoc, 3, GL_FLOAT, false, SIZEOF_MODEL_VERTEX, NORMAL_OFFSET);
        
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
            t += 0.02f;
        }

        glPolygonMode(GL_FRONT_AND_BACK, mode);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Matrix4f projection = new Matrix4f()
                .perspective((float) Math.toRadians(60f), width / (float) height, 1, 500);

        Matrix4f view = new Matrix4f()
                .lookAt(camera.getEyePosition(), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));

        
        
        Material matScene = new Material(new Vector3f(0.25f), new Vector3f(0.15f), new Vector3f(0.26f, 0.14f, 0.09f), 12.8f);
        drawModel(new Matrix4f().translate(0, -15, -5).scale(6f), view, projection, sceneArray, scene.getTriangleCount() * 3, matScene);
        
        Material matSeat = new Material(new Vector3f(0.25f), new Vector3f(0.15f), new Vector3f(0.26f, 0.14f, 0.09f), 12.8f);
        drawModel(new Matrix4f().scale(0.1f).translate(130, -80, 0), view, projection, seatArray, seat.getTriangleCount() * 3, matSeat);
        //creating material
        Material matBalCenter = new Material(new Vector3f(0.33f, 0.22f, 0.03f), new Vector3f(0.78f, 0.57f, 0.11f), new Vector3f(0.99f, 0.94f, 0.81f), 27.90f);
        drawModel(new Matrix4f().translate(0, -5, -5).rotate(t, 0f, 1f, 0f), view.rotate(t, 0f, 1f, 0f), projection, ballerinaArray, ballerina.getTriangleCount() * 3, matBalCenter);

        Material matBalLeft = new Material(new Vector3f(0.21f, 0.13f, 0.05f), new Vector3f(0.71f, 0.43f, 0.18f), new Vector3f(0.39f, 0.27f, 0.17f), 25.6f);
        drawModel(new Matrix4f().translate(-5, -5, 0).rotate(-30, 0f, 1f, 0f).rotate(t, 0f, 1f, 0f), view, projection, ballerinaArray, ballerina.getTriangleCount() * 3, matBalLeft);

        Material matBalRight = new Material(new Vector3f(0.25f), new Vector3f(0.4f), new Vector3f(0.26f, 0.14f, 0.09f), 12.8f);
        drawModel(new Matrix4f().translate(5, -5, 0).rotate(30, 0f, 1f, 0f).rotate(t, 0f, 1f, 0f), view, projection, ballerinaArray, ballerina.getTriangleCount() * 3, matBalRight);
        
        
        
        
    }
    
    
    private void drawModel(Matrix4f model, Matrix4f view, Matrix4f projection, int vao, int count) {
        drawModel(model, view, projection, vao, count, null);
    }

    private void drawModel(Matrix4f model, Matrix4f view, Matrix4f projection, int vao, int count, Material material) {
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

        //setting values for light 
        //light as a point light
        glUniform4f(lightPositionLoc, 0, 40, 0, 1);
        glUniform3f(lightAmbientColorLoc, 0.15f, 0.15f, 0.15f);
        glUniform3f(lightDiffuseColorLoc, 1, 1, 1);
        glUniform3f(lightSpecularColorLoc, 1, 1, 1);

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

}
