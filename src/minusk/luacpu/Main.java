package minusk.luacpu;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by MinusKelvin on 11/19/15.
 */
public class Main {
	public static void main(String[] args) throws FileNotFoundException {
		
		LuaTable _G = JsePlatform.standardGlobals();
		_G.set("luajava", LuaValue.NIL);
		_G.set("require", LuaValue.NIL);
		_G.set("module", LuaValue.NIL);
		_G.set("loadfile", LuaValue.NIL);
		_G.set("dofile", LuaValue.NIL);
		_G.set("collectgarbage", LuaValue.NIL);
		_G.set("io", LuaValue.NIL);
		_G.set("os", LuaValue.NIL);
//		_G.set("print", LuaValue.NIL);
		_G.set("package", LuaValue.NIL);
		_G.load(new FsApi());
		_G.load(new OsApi());
		_G.load(new ScreenApi());
		_G.load(new BitApi());
		
		Scanner scn = new Scanner(new File("bios.lua")).useDelimiter("\\Z");
		LuaValue bios = _G.get("loadstring").call(LuaValue.valueOf(scn.next()));
		scn.close();
		bios.setfenv(_G);
		bios.call();
	}
}
