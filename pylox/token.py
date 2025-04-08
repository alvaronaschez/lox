from dataclasses import dataclass
from typing import Any

@dataclass
class Token:
    type_: str
    lexeme: str
    literal: Any
    line: int
