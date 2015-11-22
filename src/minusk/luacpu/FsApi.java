package minusk.luacpu;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.io.*;
import java.util.regex.Pattern;

/**
 * Created by MinusKelvin on 11/19/15.
 */
public class FsApi extends ZeroArgFunction {
	@Override
	public LuaValue call() {
		LuaTable fsApi = new LuaTable();
		
		fsApi.set("open", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue file, LuaValue mode) {
				String path = file.strvalue().toString();
				if (!isAbsolutePath(path))
					throw new LuaError("path not absolute", 2);
				
				String m;
				if (mode.isnil())
					m = "r";
				else
					m = mode.strvalue().toString();
				
				return new FileObject(path, m);
			}
		});
		
		fsApi.set("close", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue file) {
				if (file instanceof FileObject)
					((FileObject) file).close();
				else
					throw new LuaError("not a file object", 2);
				return LuaValue.NIL;
			}
		});
		
		fsApi.set("list", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue dir) {
				String path = dir.strvalue().toString();
				if (!isAbsolutePath(path))
					throw new LuaError("path not absolute", 2);
				
				File file = new File(System.getProperty("user.dir") + "/fsroot" + path);
				if (!file.isDirectory())
					throw new LuaError("not a directory", 2);
				
				LuaTable result = new LuaTable();
				String[] list = file.list();
				for (int i = 1; i <= list.length; i++)
					result.set(i, list[i-1]);
				
				return result;
			}
		});
		
		fsApi.set("isDir", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue p) {
				String path = p.strvalue().toString();
				if (!isAbsolutePath(path))
					throw new LuaError("path not absolute", 2);
				
				File file = new File(System.getProperty("user.dir") + "/fsroot" + path);
				return LuaValue.valueOf(file.isDirectory());
			}
		});
		
		fsApi.set("isFile", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue p) {
				String path = p.strvalue().toString();
				if (!isAbsolutePath(path))
					throw new LuaError("path not absolute", 2);
				
				File file = new File(System.getProperty("user.dir") + "/fsroot" + path);
				return LuaValue.valueOf(file.isFile());
			}
		});
		
		fsApi.set("exists", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue p) {
				String path = p.strvalue().toString();
				if (!isAbsolutePath(path))
					throw new LuaError("path not absolute", 2);
				
				File file = new File(System.getProperty("user.dir") + "/fsroot" + path);
				return LuaValue.valueOf(file.exists());
			}
		});
		
		fsApi.set("isAbsolutePath", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue path) {
				return LuaValue.valueOf(isAbsolutePath(path.strvalue().toString()));
			}
		});
		
		env.set("fs", fsApi);
		return LuaValue.NIL;
	}
	
	private Pattern pattern = Pattern.compile("/\\.\\.?(/|$)");
	private boolean isAbsolutePath(String path) {
		return !(!path.startsWith("/") || path.contains("//")) && !pattern.matcher(path).matches();
	}
	
	private class FileObject extends LuaTable {
		private final String path;
		private final BufferedInputStream in;
		private final BufferedOutputStream out;
		
		private FileObject(String path, String mode) {
			File file = new File(System.getProperty("user.dir") + "/fsroot" + path);
			this.path = path;
			
			try {
				switch (mode) {
					case "r":
						if (!file.isFile())
							throw new LuaError("not a file", 2);
						in = new BufferedInputStream(new FileInputStream(file));
						out = null;
						break;
					case "w":
						if (file.isDirectory())
							throw new LuaError("not a file", 2);
						in = null;
						out = new BufferedOutputStream(new FileOutputStream(file));
						break;
					case "a":
						if (!file.isFile())
							throw new LuaError("not a file", 2);
						in = null;
						out = new BufferedOutputStream(new FileOutputStream(file, true));
						break;
					default:
						throw new LuaError("invalid mode", 2);
				}
			} catch (IOException e) {
				throw new LuaError(e);
			}
			
			set("close", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					close();
					return LuaValue.NIL;
				}
			});
			
			set("readByte", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return readByte();
				}
			});
			
			set("readAll", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return readAll();
				}
			});
			
			set("isWriting", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return LuaValue.valueOf(writing());
				}
			});
			
			set("isReading", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return LuaValue.valueOf(reading());
				}
			});
			
			set("writeByte", new OneArgFunction() {
				@Override
				public LuaValue call(LuaValue b) {
					return writeByte(b.checkint());
				}
			});
			
			set("writeString", new OneArgFunction() {
				@Override
				public LuaValue call(LuaValue s) {
					return writeString(s.checkjstring());
				}
			});
		}
		
		private void close() {
			try {
				if (in != null)
					in.close();
				else
					out.close();
			} catch (IOException e) {
				
			}
		}
		
		private LuaValue readByte() {
			if (!reading())
				throw new LuaError("cannot read from file opened for writing", 2);
			
			int c;
			try {
				c = in.read();
			} catch (IOException e) {
				c = -1;
			}
			return LuaValue.valueOf(c);
		}
		
		private LuaValue readAll() {
			if (!reading())
				throw new LuaError("cannot read from file opened for writing", 2);
			
			try {
				StringBuilder s = new StringBuilder();
				int c;
				while ((c = in.read()) != -1)
					s.append((char) c);
				return LuaValue.valueOf(s.toString());
			} catch (IOException e) {
				return LuaValue.valueOf("");
			}
		}
		
		private LuaValue writeByte(int c) {
			try {
				out.write(c);
				return LuaValue.TRUE;
			} catch (IOException e) {
				return LuaValue.FALSE;
			}
		}
		
		private LuaValue writeString(String c) {
			try {
				out.write(c.getBytes());
				return LuaValue.TRUE;
			} catch (IOException e) {
				return LuaValue.FALSE;
			}
		}
		
		private boolean writing() {
			return out != null;
		}
		
		private boolean reading() {
			return in != null;
		}
	}
}
