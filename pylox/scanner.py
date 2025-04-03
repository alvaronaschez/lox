import re

#pattern = r"""(?x) # verbose mode
#    and|class|else|false|for|fun|if|nil|or|print|return|super|this|true|var|while # keywords
#    |==|!=|<=|>= # two character tokens
#    |\(|\)|\{|\}|,|\.|-|\+|;|\*|!|=|<|> # one character tokens
#    |(?://.*$) # comments
#    |\s+|\r+|\t+ # meaningless characters
#    |\".*\" # string literal
#    |\d+(?:\.\d+)* # number
#    |[a-zA-Z_]+[a-zA-Z0-9_]* # identifier
#    """

keywords = ["and", "class", "else", "false", "for", "fun", "if", "nil", "or",
            "print", "return", "super", "this", "true", "var", "while"]

two_character_tokens = ["==", "!=", "<=", ">="]

one_character_tokens = ["(", ")", "{", "}", ",", ".", "-", "+", ";", "*", "!", "=", "<", ">"]
one_character_tokens = [re.escape(token) for token in one_character_tokens]

comments = "(?://.*$)"
meaningless_characters = r"\s+|\r+|\t+"
string_literal = r'\".*\"'
number = r"\d+(?:\.\d+)*"
identifier = r"[a-zA-Z_]+[a-zA-Z0-9_]*"


pattern = "|".join(
        keywords + two_character_tokens + one_character_tokens
        + [comments, meaningless_characters, string_literal, number, identifier])

#regex = re.compile(pattern, re.MULTILINE)
regex = re.compile(pattern)
discard = re.compile(f"{meaningless_characters}|{comments}")



def scan_line(line: str) -> list[str]:
    """
    >>> scan_line("(1.5 and hello) //  comment ") 
    ['(', '1.5', 'and', 'hello', ')']

    >>> scan_line("//1.5 and hello //  comment ") 
    []

    >>> scan_line("1 and 2.7 @ // comment")
    Traceback (most recent call last):
        ...
    Exception: scanner error

    >>> scan_line('"1.5 and hello //  comment ') 
    Traceback (most recent call last):
        ...
    Exception: scanner error
    """
    tokens = regex.findall(line)
    if len("".join(tokens)) < len(line):
        raise Exception("scanner error")
    tokens = [token for token in tokens if not discard.fullmatch(token)]
    return tokens


def scan(source: str) -> list[str]:
    tokens = regex.findall(source)
    if len("".join(tokens)) < len(source):
        raise Exception("scanner error")
    return tokens


if __name__ == "__main__":
    import doctest
    doctest.testmod()

