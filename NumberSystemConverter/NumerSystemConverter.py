def binary_to_decimal(binary_str):
    if binary_str.startswith("0b"):
        binary_str = binary_str[2:]
    
    total = 0
    str_length = len(binary_str)
    for i, digit in enumerate(binary_str):
        if digit not in "01":
            raise ValueError("Invalid binary number")
        if digit == '1':
            n = str_length - i - 1
            total += int(digit) * pow(2, n)
    return total

def decimal_to_binary(n):
    if n == 0:
        return "0b0"
    
    binary_str = ""
    while n > 0:
        remainder = n % 2
        binary_str = str(remainder) + binary_str
        n = n // 2
    return "0b" + binary_str

def octal_to_decimal(octal_str):
    if octal_str.startswith("0o"):
        octal_str = octal_str[2:]
    
    total = 0
    str_length = len(octal_str)
    for i, digit in enumerate(octal_str):
        if digit not in "01234567":
            raise ValueError("Invalid octal number")
        n = str_length - i - 1
        total += int(digit) * pow(8, n)
    return total

def decimal_to_octal(n):
    if n == 0:
        return "0o0"
    
    octal_str = ""
    while n > 0:
        remainder = n % 8
        octal_str = str(remainder) + octal_str
        n = n // 8
    return "0o" + octal_str

def hex_to_decimal(hex_str):
    if hex_str.startswith("0x"):
        hex_str = hex_str[2:]
    
    total = 0
    str_length = len(hex_str)
    hex_digits = "0123456789ABCDEF"
    
    for i, digit in enumerate(hex_str.upper()):
        if digit not in hex_digits:
            raise ValueError("Invalid hexadecimal number")
        n = str_length - i - 1
        total += hex_digits.index(digit) * pow(16, n)
    return total

def decimal_to_hex(n):
    if n == 0:
        return "0x0"
    
    hex_str = ""
    hex_digits = "0123456789ABCDEF"
    
    while n > 0:
        remainder = n % 16
        hex_str = hex_digits[remainder] + hex_str
        n = n // 16
    return "0x" + hex_str

def detect_base(token: str):
    if token.startswith("0b"):
        return 2
    elif token.startswith("0o"):
        return 8
    elif token.startswith("0x"):
        return 16
    else:
        return 10

def validate(s: str, base: int):
    if base == 2:
        valid_chars = "01"
    elif base == 8:
        valid_chars = "01234567"
    elif base == 10:
        if s.startswith("-"):
            s = s[1:]
        valid_chars = "0123456789"
    elif base == 16:
        valid_chars = "0123456789ABCDEFabcdef"
    else:
        return False
    
    if s.startswith(("0b", "0o", "0x")):
        s = s[2:]
    
    for char in s:
        if char not in valid_chars:
            return False
    return True

def convert(num_str: str):
    base = detect_base(num_str)
    if not validate(num_str, base):
        print("Invalid number for the detected base.")
        return

    if base == 2:
        decimal_value = binary_to_decimal(num_str)
    elif base == 8:
        decimal_value = octal_to_decimal(num_str)
    elif base == 10:
        decimal_value = int(num_str)
    elif base == 16:
        decimal_value = hex_to_decimal(num_str)
    else:
        print("Unsupported base.")
        return
    
    print(f"Decimal: {decimal_value}")
    print(f"Binary: {decimal_to_binary(decimal_value)}")
    print(f"Octal: {decimal_to_octal(decimal_value)}")
    print(f"Hexadecimal: {decimal_to_hex(decimal_value)}")

def main():
    print("===================== Number System Converter =====================")
    print("Enter a number with its base prefix (0b for binary, 0o for octal, 0x for hexadecimal, no prefix for decimal).")
    print("Type 'exit' to quit.")
    
    while True:
        user_input = input("Enter number: ").strip()
        if user_input.lower() == 'exit':
            break
        try:
            convert(user_input)
        except ValueError as e:
            print(e)
        except Exception as e:
            print(f"An error occurred: {e}")
        else:   
            print("Conversion successful.")
if __name__ == "__main__":
    main()
