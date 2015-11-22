screen.update()

-- Fix LuaJ's broken string.sub and string.find
do
	local nativestringfind = string.find
	local nativestringsub = string.sub
	local nativepcall = pcall
	function string.sub(s, start, nd)
		local ok, r = nativepcall(nativestringsub, s, start, nd)
		if ok then
			if r then
				return r .. ""
			end
			return nil
		end
		error(r, 2)
	end
	function string.find(s, ...)
		return nativestringfind(s .. "", ...)
	end
end

-- Prevent access to string metatable or environment, since this could mess stuff up
do
	local nativegetmetatable = getmetatable
	local stringmetatable = nativegetmetatable("")
	local nativeerror = error
	function getmetatable(t)
		local metatable = nativegetmetatable(t)
		if metatable == stringmetatable then
			nativeerror("cannot access string metatable", 2)
		end
		return metatable
	end
	
	local nativegetfenv = getfenv
	local stringenv = nativegetfenv(("").gsub)
	local nativetype = type
	function getfenv(env)
		if env == nil then
			env = 2
		elseif nativetype(env) == 'number' and env > 0 then
			env = env + 1
		end
		local fenv = nativegetfenv(env)
		if fenv == stringenv then
			return nativegetfenv(0)
		end
		return fenv
	end
end

local file = fs.open("/boot/kernel")
local func = loadstring(file.readAll(), "kernel")
file.close()

xpcall(func, function(err)
	print(err)
end)
