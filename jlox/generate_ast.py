# from collections import OrderedDict
from jinja2 import Environment, FileSystemLoader, select_autoescape

types = {
    "Unary": ["Token operator", "Expr right",],
    "Binary": ["Expr left", "Token operator", "Expr right",],
    "Grouping": ["Expr expression",],
    "Literal": ["Object value",],
}

# types = OrderedDict([
#     ("Unary", ["Token operator", "Expr right",]),
#     ("Binary", ["Expr left", "Token operator", "Expr right",]),
#     ("Grouping", ["Expr expression",]),
#     ("Literal", ["Object value",]),
# ])

env = Environment(
    loader=FileSystemLoader("."),
    autoescape=select_autoescape(),
    trim_blocks=True,
    lstrip_blocks=True,
)

template = env.get_template("Expr.java.jinja")

r = template.render(base_name="Expr", types=types)

print(r)

with open("Expr.java", "w") as f:
    f.write(r)

