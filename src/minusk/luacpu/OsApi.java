package minusk.luacpu;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;

import java.util.ArrayDeque;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Created by MinusKelvin on 11/20/15.
 */
public class OsApi extends ZeroArgFunction {
	private static long window;
	private static final ArrayDeque<LuaTable> eventQueue = new ArrayDeque<>();
	private static int ticks = 0;
	
	private static GLFWKeyCallback kcb;
	
	@Override
	public LuaValue call() {
		LuaTable osApi = new LuaTable();
		final LuaValue _G = env;
		
		glfwInit();
		
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, 1);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		
		window = glfwCreateWindow(800, 602, "lua-cpu", 0, 0);
		
		glfwMakeContextCurrent(window);
		glfwSwapInterval(0);
		GL.createCapabilities();
		
		glfwSetKeyCallback(window, kcb = GLFWKeyCallback(OsApi::kcb));
		
		osApi.set("time", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(System.currentTimeMillis() / 1000.0);
			}
		});
		
		osApi.set("pullEvent", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				while (eventQueue.size() == 0)
					glfwWaitEvents();
				synchronized (eventQueue) {
					return _G.get("unpack").invoke(eventQueue.removeFirst());
				}
			}
		});
		
		osApi.set("queueEvent", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				eventQueue.addLast(new LuaTable(args));
				return LuaValue.NIL;
			}
		});
		
		osApi.set("startTimer", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue time, LuaValue tbl) {
				int id = ticks++;
				double d = time.checkdouble();
				Thread thread = new Thread(() -> {
					try {
						Thread.sleep((long)(1000*d));
						LuaTable table = new LuaTable();
						table.set(1, LuaValue.valueOf("timer"));
						table.set(2, LuaValue.valueOf(id));
						table.set(3, tbl);
						synchronized (eventQueue) {
							eventQueue.add(table);
						}
						glfwPostEmptyEvent();
					} catch (Exception e) {}
				});
				thread.setDaemon(true);
				thread.start();
				return LuaValue.valueOf(id);
			}
		});
		
		env.set("os", osApi);
		return LuaValue.NIL;
	}
	
	public static void swap() {
		glfwSwapBuffers(window);
	}
	
	private static void kcb(long window, int key, int scancode, int action, int mods) {
		if (action == GLFW_REPEAT) return;
		LuaTable table = new LuaTable();
		table.set(1, LuaValue.valueOf("key"));
		table.set(2, LuaValue.valueOf(key));
		table.set(3, LuaValue.valueOf(action==GLFW_PRESS));
		table.set(4, LuaValue.valueOf(mods));
		eventQueue.addLast(table);
	}
}
