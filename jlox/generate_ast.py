from jinja2 import Environment, BaseLoader, select_autoescape

template = """\
package jlox;

import java.util.List;

abstract class {{base_name}} {

	interface Visitor<R> {
		{% for type_name in types.keys() %}
		R visit{{type_name}}{{base_name}}({{type_name}} {{base_name.lower()}});
		{% endfor %}
	}

	abstract <R> R accept(Visitor<R> visitor);

	{% for type_name, attributes in types.items() %}
	static class {{type_name}} extends {{base_name}} {
		{# attributes #}
		{% for atr in attributes %}
		final {{ atr }};
		{% endfor %}

		{# constructor #}
		{{ type_name }}({{", ".join(attributes)}}) {
			{% for atr in attributes %}
				{% set atr_name = atr.split(" ")[1] %}
			this.{{atr_name}} = {{ atr_name}};
			{% endfor %}
		}

		@Override <R> R accept(Visitor<R> visitor) {
			return visitor.visit{{type_name}}{{base_name}}(this);
		}
	}

	{% endfor %}
}
"""

types = {
    "Stmt":{
        "Expression": ["Expr expression",],
        "Print": ["Expr expression",],
        "Var": ["Token name", "Expr initializer",],
    },
    "Expr":{
        "Assign": ["Token name", "Expr value",],
        "Variable": ["Token name",],
        "Unary": ["Token operator", "Expr right",],
        "Binary": ["Expr left", "Token operator", "Expr right",],
        "Grouping": ["Expr expression",],
        "Literal": ["Object value",],
    },
}

rtemplate = Environment(
    loader=BaseLoader,
    autoescape=select_autoescape(),
    trim_blocks=True,
    lstrip_blocks=True,
    ).from_string(template)

for k, v in types.items():
    r = rtemplate.render(base_name=k, types=v)

    print(r)

    with open(f"{k}.java", "w") as f:
        f.write(r)

