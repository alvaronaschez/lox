
--[[
/*
 * expression -> literal
 * 						| unary
 * 						| binary
 * 						| grouping ;
 *
 * literal	-> NUMBER | STRING | "true" | "false" | "nil" ;
 * grouping	-> "(" expression ")" ;
 * unary		-> ( "-" | "!" ) expression ;
 * binary		-> expression operator expression ;
 * operator	-> "==" | "!=" | "<" | "<=" | ">" | ">=" 
 * 					| "+" | "-" | "*" | "/" ;
 *
 */
 --]]

--local path = "./Expr.java"
--local file = assert(io.open(path, "w"))
--file:write("hola")
--file:close()

local define_ast, define_visitor, define_type

local function main()
	print "Generating 'Expr.java' ...\n"
	local output_dir = "."
	local types = {
		Binary		= {"Expr left", "Token operator", "Expr right"},
		Grouping	= {"Expr expression"},
		Literal		= {"Object value"},
		Unary			= {"Token operator", "Expr right"},
	}
	define_ast(output_dir, "Expr", types)
end

define_ast = function(output_dir, base_name, types)
	local path = output_dir .. "/" .. base_name .. ".java"
	local writer = assert(io.open(path, "w"))
	writer:write(
		"package jlox;\n" ..
		"\n" ..
		"import java.util.List;\n" ..
		"\n" ..
		"abstract class " .. base_name .. " {\n\n")

	define_visitor(writer, base_name, types)

	writer:write(
		"\tabstract <R> R accept(Visitor<R> visitor);\n\n")

	for key, val in pairs(types) do
		define_type(writer, base_name, key, val)
	end

	writer:write(
		"}\n")

	writer:close()
end

define_visitor = function(writer, base_name, types)
	writer:write(
		"\tinterface Visitor<R> {\n")
	for type_name, _ in pairs(types) do
		writer:write(
			"\t\tR visit" ..  type_name .. base_name .. "(" .. type_name .. " " .. base_name:lower() .. ");\n")
	end
	writer:write(
		"\t}\n\n")
end

define_type = function(writer, base_name, class_name, fields)
	local field_list = ""
	for _, field in pairs(fields) do field_list = field_list .. field .. ", " end
	field_list = string.sub(field_list, 1, -3) -- remove trailing comma

	-- class definition
	writer:write(
		"\tstatic class " .. class_name .. " extends " .. base_name .. " {\n")

	-- attribute declarations
	for _, field in pairs(fields) do
		writer:write(
			"\t\tfinal " .. field .. ";\n")
	end

	writer:write(
		"\n")

	-- constructor
	writer:write(
		"\t\t" .. class_name .. "(" .. field_list .. ") {\n")
	-- store parameters in fields
	for _, field in pairs(fields) do
		local field_name = string.gsub(field, ".+ ", "")
		writer:write(
			"\t\t\tthis." .. field_name .. " = " .. field_name .. ";\n")
	end
	-- close constructor
	writer:write(
		"\t\t}\n")

	writer:write(
		"\n")

	-- implement accept abstract method
	writer:write(
		"\t\t@Override <R> R accept(Visitor<R> visitor) {\n" ..
		"\t\t\treturn visitor.visit".. class_name .. base_name .."(this);\n" ..
		"\t\t}\n"
	)

	-- close class definition
	writer:write(
		"\t}\n\n")
end

main()

