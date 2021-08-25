package gerta;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import renderer.DebugDraw;
import scenes.LevelEditorScene;
import scenes.LevelScene;
import scenes.Scene;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    /**
     *
     */
    private int width;
    private int height;
    private final String title;
    private long glfwWindow;
    private ImGuiLayer imguiLayer;

    public float r, g, b, a;

    private static Window window;

    private static Scene currentScene;

    static {
        window = null;
    }

    private Window() {
        this.width = 800;
        this.height = 600;
        this.title = "My Game" ;
        r = 1;
        b = 1;
        g = 1;
        a = 1;
    }

    public static void changeScene(int newScene){
        switch (newScene){
            case 0:
                currentScene = new LevelEditorScene();
                break;
            case 1:
                currentScene = new LevelScene();
                break;
            default:
                assert false: "Unknown scene '" + newScene + "'";
                break;
        }
        currentScene.load();
        currentScene.init();
        currentScene.start();
    }

    public static  Window get(){
        if (Window.window == null){
            Window.window = new Window();
        }
        return Window.window;
    }

    public  static Scene getScene(){
        return get().currentScene;
    }

    public void run () {
        System.out.println("Hello LWJGL" + Version.getVersion()+ "!");

        init();
        loop();

        /*
         Free the memory
         */
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        /*
          Terminate GLFW and the free the error callback
         */
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /*
      Setup and error callback
     */
    public void init(){
        GLFWErrorCallback.createPrint(System.err).set();

        /*
         Initialize GLFW
         */
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW.");
        }
        /*
         Configure GLFW
         */
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        /*
          Create the window
         */
        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if(glfwWindow == NULL){
            throw new IllegalStateException("Failed to create the GLFW window.");
        }

        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);
        glfwSetWindowSizeCallback(glfwWindow, (w, newWidth, newHeight) ->{
            Window.setWidth(newWidth);
            Window.setHeight(newHeight);
        });

        /*
          Make the OpenGL context current
          glfwSwapInterval(1) turn On v-sync
         */
        glfwMakeContextCurrent(glfwWindow);
        glfwSwapInterval(1);

        /*
          Make the window visible
         */
        glfwShowWindow(glfwWindow);

        /*
          This line is critical for LWJGL's interoperation with GLFW's
          OpenGL context, or any context that is managed externally.
          LWJGL detects the context that is current in the current thread,
          creates the GLCapabilities instance and makes the OpenGL
          bindings available for use.
         */
        GL.createCapabilities();

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        this.imguiLayer = new ImGuiLayer(glfwWindow);
        this.imguiLayer.initImGui();

        Window.changeScene(0);
    }

    public void loop(){
        float beginTime =(float)glfwGetTime();
        float endTime;
        float dt = -1.0f;

        while (!glfwWindowShouldClose(glfwWindow)){
            //Poll events
            glfwPollEvents();

            DebugDraw.beginFrame();

            glClearColor(r, g, b, a);
            glClear(GL_COLOR_BUFFER_BIT);

            if (dt >= 0) {
                DebugDraw.draw();
                currentScene.update(dt);
            }

            if (KeyListener.isKeyPressed(GLFW_KEY_ESCAPE)){
                glfwSetWindowShouldClose(glfwWindow, true);
            }

            this.imguiLayer.update(dt, currentScene);
            glfwSwapBuffers(glfwWindow);

            endTime =(float)glfwGetTime();
            dt = endTime - beginTime;
            beginTime = endTime;
        }
        currentScene.saveExit();
    }

    public static int getWidth(){
        return get().width;
    }

    public static int getHeight(){
        return get().height;
    }

    public static void setWidth(int newWidth){
        get().width = newWidth;
    }

    public static void setHeight(int newHeight){
        get().height = newHeight;
    }
}
