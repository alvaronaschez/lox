from dataclasses import dataclass
from typing import Any

from .token import Token


@dataclass
class Expr:
    ...

@dataclass
class Unary(Expr):
    operator: Token
    right: Expr

@dataclass
class Binary(Expr):
    left: Expr
    operator: Token
    right: Expr

@dataclass
class Grouping(Expr):
    expression: Expr

@dataclass
class Literal(Expr):
    value: Any

