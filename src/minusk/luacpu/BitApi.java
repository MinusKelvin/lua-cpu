package minusk.luacpu;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
 * Created by MinusKelvin on 11/22/15.
 */
public class BitApi extends ZeroArgFunction {
	@Override
	public LuaValue call() {
		LuaTable bitApi = new LuaTable();
		
		bitApi.set("bnot", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				return LuaValue.valueOf(~arg.checkint());
			}
		});
		
		bitApi.set("bor", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				return LuaValue.valueOf(arg1.checkint() | arg2.checkint());
			}
		});
		
		bitApi.set("band", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				return LuaValue.valueOf(arg1.checkint() & arg2.checkint());
			}
		});
		
		bitApi.set("bxor", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				return LuaValue.valueOf(arg1.checkint() ^ arg2.checkint());
			}
		});
		
		bitApi.set("brshift", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				return LuaValue.valueOf(arg1.checkint() >> arg2.checkint());
			}
		});
		
		bitApi.set("blshift", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				return LuaValue.valueOf(arg1.checkint() << arg2.checkint());
			}
		});
		
		bitApi.set("blrshift", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				return LuaValue.valueOf(arg1.checkint() >>> arg2.checkint());
			}
		});
		
		env.set("bit", bitApi);
		return LuaValue.NIL;
	}
}
