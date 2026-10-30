#!/usr/bin/env bash

set -euo pipefail

# --------------------------------
# VARIABLES
# --------------------------------
NAME="${1:-Jim}"

echo "============================"
echo "Bash Script Demo"
echo "============================"

# --------------------------------
# FOR LOOP - Numeric range
# --------------------------------
echo
echo "1. FOR loop"

for i in {1..5}; do
    echo "Count: $i"
done

# --------------------------------
# FOR LOOP - Array
# --------------------------------
echo
echo "2. FOR loop - Array"

FRUITS=("Apple" "Orange" "Banana")

for fruit in "${FRUITS[@]}"; do
    echo "Fruit: $fruit"
done

# --------------------------------
# WHILE LOOP
# --------------------------------
echo
echo "3. WHILE loop"

count=1

while (( count <= 5 )); do
    echo "While count: $count"
    ((count++)) || true
done

# --------------------------------
# UNTIL LOOP
# --------------------------------
echo
echo "4. UNTIL loop"

count=1

until (( count > 5 )); do
    echo "Until count: $count"
    ((count++)) || true
done

# --------------------------------
# CASE STATEMENT
# --------------------------------
echo
echo "5. CASE statement"

choice="2"

case "$choice" in
    1)
        echo "Option 1 selected"
        ;;
    2)
        echo "Option 2 selected"
        ;;
    3)
        echo "Option 3 selected"
        ;;
    *)
        echo "Unknown option"
        ;;
esac

# --------------------------------
# IF / ELSE
# --------------------------------
echo
echo "6. IF / ELSE"

if [[ -f /etc/os-release ]]; then
    echo "Linux distribution information found"
else
    echo "Linux distribution information missing"
fi

# --------------------------------
# FUNCTION
# --------------------------------
echo
echo "7. Function"

greet() {
    local name="$1"
    echo "Hello, $name!"
}

greet "$NAME"

# --------------------------------
# COMMAND-LINE ARGUMENTS
# --------------------------------
echo
echo "8. Arguments"

echo "Script name: $0"
echo "Argument count: $#"
echo "First argument: ${1:-None}"

# --------------------------------
# EXIT STATUS
# --------------------------------
echo
echo "9. Exit status"

if grep -q "Linux" /etc/os-release; then
    echo "Linux detected"
    echo "Exit code: $?"
fi

echo
echo "Demo complete."
