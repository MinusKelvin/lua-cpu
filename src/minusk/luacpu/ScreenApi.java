package minusk.luacpu;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Created by MinusKelvin on 11/19/15.
 */
public class ScreenApi extends ZeroArgFunction {
	@Override
	public LuaValue call() {
		LuaTable screenApi = new LuaTable();
		
		glBindVertexArray(glGenVertexArrays());
		int tex = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, tex);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 800, 602, 0, GL_BGRA, GL_FLOAT, 0);
		IntBuffer buffer = BufferUtils.createIntBuffer(800*602);
		int s1 = glCreateShader(GL_VERTEX_SHADER), s2 = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(s1, "#version 330 core\nconst vec2[] verts=vec2[4](vec2(-1.0,-1.0),vec2(1.0,-1.0),vec2(-1.0,1.0),vec2(1.0,1.0));"
				+ "out vec2 tex;void main(){gl_Position=vec4(verts[gl_VertexID],0.0,1.0);tex=verts[gl_VertexID]/2.0;tex.x+=0.5;tex.y+=0.5;}");
		glShaderSource(s2, "#version 330 core\nin vec2 tex;out vec4 col;uniform sampler2D textures;void main(){col=texture(textures,tex);}");
		glCompileShader(s1);
		glCompileShader(s2);
		int prog = glCreateProgram();
		glAttachShader(prog, s1);
		glAttachShader(prog, s2);
		glLinkProgram(prog);
		glUseProgram(prog);
		glDeleteShader(s2);
		glDeleteShader(s1);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		
		screenApi.set("setPixel", new ThreeArgFunction() {
			@Override
			public LuaValue call(LuaValue _x, LuaValue _y, LuaValue _color) {
				int x = _x.checkint() - 1;
				int y = _y.checkint() - 1;
				int color = _color.checkint();
				
				if (x < 0 || x >= 800)
					throw new LuaError("x not in range 1-800 inclusive", 2);
				if (y < 0 || y >= 602)
					throw new LuaError("y not in range 1-602 inclusive", 2);
				
				buffer.put(x+y*800, color);
				return LuaValue.NIL;
			}
		});
		
		screenApi.set("getPixel", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue _x, LuaValue _y) {
				int x = _x.checkint() - 1;
				int y = _y.checkint() - 1;
				
				if (x < 0 || x >= 800)
					throw new LuaError("x not in range 1-800 inclusive", 2);
				if (y < 0 || y >= 602)
					throw new LuaError("y not in range 1-602 inclusive", 2);
				
				return LuaValue.valueOf(buffer.get(x+y*800));
			}
		});
		
		screenApi.set("update", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				buffer.position(0);
				glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 800, 602, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
				glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
				OsApi.swap();
				return LuaValue.NIL;
			}
		});
		
		env.set("screen", screenApi);
		return LuaValue.NIL;
	}
}
