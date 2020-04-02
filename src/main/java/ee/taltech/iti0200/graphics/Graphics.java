package ee.taltech.iti0200.graphics;

import com.google.inject.Inject;
import ee.taltech.iti0200.application.Component;
import ee.taltech.iti0200.di.annotations.WindowId;
import ee.taltech.iti0200.di.factory.RendererFactory;
import ee.taltech.iti0200.domain.World;
import ee.taltech.iti0200.domain.entity.Bot;
import ee.taltech.iti0200.domain.entity.Entity;
import ee.taltech.iti0200.domain.entity.FastGun;
import ee.taltech.iti0200.domain.entity.Gun;
import ee.taltech.iti0200.domain.entity.Living;
import ee.taltech.iti0200.domain.entity.Player;
import ee.taltech.iti0200.physics.Body;
import ee.taltech.iti0200.physics.BoundingBox;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static ee.taltech.iti0200.graphics.Camera.INITIAL_ZOOM_VALUE;
import static java.util.stream.Collectors.toMap;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * Mostly still the  hello-world example from https://www.lwjgl.org/guide just to get
 * a rough idea on how to start using LWJGL library
 */
public class Graphics implements Component {

    public static final String DEFAULT = "DEFAULT";
    public static final HashMap<Class<? extends Body>, HashMap<String, Supplier<Renderer>>> renderers = new HashMap<>();

    private long window;

    private World world;
    private Shader shader;
    private Camera camera;
    private int frameHeight;
    private int frameWidth;
    private RendererFactory factory;

    @Inject
    public Graphics(World world, @WindowId long window, Camera camera, RendererFactory factory) {
        this.world = world;
        this.camera = camera;
        this.window = window;
        this.factory = factory;
    }

    @Override
    public void initialize() throws IOException {
        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);
            frameHeight = pHeight.get(0);
            frameWidth = pWidth.get(0);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                window,
                (vidmode.width() - frameHeight) / 2,
                (vidmode.height() - frameWidth) / 2
            );

            camera.setWidth(frameWidth).setHeight(frameHeight).setZoom(INITIAL_ZOOM_VALUE);

        } // the stack frame is popped automatically

        glfwSetFramebufferSizeCallback(window, (long window, int w, int h) -> {
            if (w > 0 && h > 0 && (frameWidth != w || frameHeight != h)) {
                camera.setHeight(h).setWidth(w).setZoom(camera.getZoom());
                frameWidth = w;
                frameHeight = h;
            }
        });

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shader = new Shader("shader");

        camera.setPosition(new Vector3f(0, 0, 0));

        createRenderers();

        world.getEntities().forEach(entity -> {
            setRenderer(entity);
            if (entity instanceof Living) {
                setRenderer(((Living) entity).getGun());
            }
        });

        glClearColor(0.3f, 0.3f, 0.3f, 0.0f);
    }

    @Override
    public void terminate() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public boolean isWindowOpen() {
        return !glfwWindowShouldClose(window);
    }

    @Override
    public void update(long tick) {
        glfwPollEvents();
        glViewport(0, 0, frameWidth, frameHeight);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        camera.update();

        renderInView(tick);

        glfwSwapBuffers(window); // swap the color buffers
    }

    /**
     * Render entities that happen to be in view of the camera
     * Add 10 pixel padding around the viewport to have something rendered there when traveling fast
     * Negating camera coordinates as they seem to have opposite values of the world coordinates
     */
    private void renderInView(long tick) {
        Vector3f pos = new Vector3f(camera.getPosition()).negate();
        float zoom = camera.getZoom();

        double w = 10 + camera.getWidth() / 2.0 * zoom;
        double h = 10 + camera.getHeight() / 2.0 * zoom;
        double minX = (pos.x - w);
        double maxX = (pos.x + w);
        double minY = (pos.y - h);
        double maxY = (pos.y + h);

        for (Entity entity : world.getEntities()) {
            BoundingBox box = entity.getBoundingBox();
            if (box.getMinX() < minX || box.getMaxX() > maxX || box.getMinY() < minY || box.getMinY() > maxY) {
                continue;
            }
            entity.render(shader, camera, tick);
            if (entity instanceof Living) {
                Gun gun = ((Living) entity).getGun();
                if (gun != null) {
                    gun.render(shader, camera, tick);
                }
            }
        }
    }

    private void createRenderers() throws IOException {
        Texture defaultTexture = new Texture("", "default");
        Texture gunTexture = new Texture("animations/gun/", "shotgun");
        Animation playerDefault = new Animation(2, "animations/player/", "player.default", 20);
        Animation playerJump = new Animation(2, "animations/player/", "player.jump", 20);
        Animation botDefault = new Animation(2, "animations/bot/", "bot.default", 20);

        HashMap<String, Supplier<Renderer>> defaultRenderer = new HashMap<>();
        defaultRenderer.put(DEFAULT, () -> factory.create(defaultTexture));
        renderers.put(Entity.class, defaultRenderer);

        HashMap<String, Supplier<Renderer>> gunRenderer = new HashMap<>();
        gunRenderer.put(DEFAULT, () -> factory.create(gunTexture, RotatingDrawable.class));
        renderers.put(Gun.class, gunRenderer);
        renderers.put(FastGun.class, gunRenderer);

        HashMap<String, Supplier<Renderer>> playerRenderer = new HashMap<>();
        playerRenderer.put(DEFAULT, () -> factory.create(playerDefault));
        playerRenderer.put("jump", () -> factory.create(playerJump));
        renderers.put(Player.class, playerRenderer);

        HashMap<String, Supplier<Renderer>> botRenderer = new HashMap<>();
        botRenderer.put(DEFAULT, () -> factory.create(botDefault));
        renderers.put(Bot.class, botRenderer);
    }

    public static void setRenderer(Entity entity) {
        if (renderers.isEmpty() || entity == null) {
            return;
        }

        HashMap<String, Renderer> map = renderers.getOrDefault(entity.getClass(), renderers.get(Entity.class))
            .entrySet()
            .stream()
            .collect(toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().get().setEntity(entity).initialize(),
                (a, b) -> b,
                HashMap::new
            ));
        entity.setRenderers(map);
    }

}
