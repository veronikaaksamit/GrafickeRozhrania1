package cz.muni.fi.pv112.project;

import org.lwjgl.Version;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

/**
 *
 * @author veronika aksamitova
 */
public class ProjectOpenGL {

    private Camera camera;
    
    public static void main(String[] args) {
        new ProjectOpenGL().run();
    }
    
    public void run() {
        System.out.println("Using LWJGL version:" + Version.getVersion() + "!");
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void loop() {
        // Prepare data for rendering
        init();
        // Run the rendering loop until the user has attempted to close it pressing the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            render();

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }
}
