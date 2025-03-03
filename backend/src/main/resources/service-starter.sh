#!/bin/bash

file="%s"

dir=$(dirname "$file")

shift

args_string="%s"

set -- $args_string

args=("$@")

if [[ -x "$file" && ! -d "$file" ]]; then
    nohup "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 &
    pid=$!
    echo "$pid"
    exit
fi

filetype=$(file -b "$file")

if [[ "$file" =~ \.jar$ ]]; then
    filetype="Java archive"
fi

case "$filetype" in
    *"Java archive"*)
        nohup java -jar "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 &
        pid=$!
        echo "$pid"
        ;;
    *"Python script"*)
        nohup python3 "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 &
        pid=$!
        echo "$pid"
        ;;
    *"Node.js script"*)
        nohup node "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 &
        pid=$!
        echo "$pid"
        ;;
    *"Perl script"*)
        nohup perl "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 &
        pid=$!
        echo "$pid"
        ;;
    *"Ruby script"*)
        nohup ruby "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 &
        pid=$!
        echo "$pid"
        ;;
    *"shell script"*)
        nohup bash "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 &
        pid=$!
        echo "$pid"
        ;;
    *"text"*)
        shebang=$(head -n 1 "$file" | cut -d' ' -f1)

        case "$shebang" in
            "#!/usr/bin/env python"*)    nohup python3 "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 & pid=$!; echo "$pid" ;;
            "#!/usr/bin/env node"*)      nohup node "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 & pid=$!; echo "$pid" ;;
            "#!/usr/bin/env perl"*)      nohup perl "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 & pid=$!; echo "$pid" ;;
            "#!/usr/bin/env ruby"*)      nohup ruby "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 & pid=$!; echo "$pid" ;;
            "#!/bin/bash"*)              nohup bash "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 & pid=$!; echo "$pid" ;;
            "#!/bin/sh"*)                nohup sh "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 & pid=$!; echo "$pid" ;;
            "#!/usr/bin/env php"*)       nohup php "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 & pid=$!; echo "$pid" ;;
            "#!/usr/bin/env go"*)        nohup go run "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 & pid=$!; echo "$pid" ;;
            "#!/usr/bin/env rust"*)      nohup rustc "$file" && ./$(basename "$file" .rs) "${args[@]}" >> "$dir/nohup.out" 2>&1 & pid=$!; echo "$pid" ;;
            *) exit 1 ;;
        esac
        ;;
    *"ELF"*)
        nohup "$file" "${args[@]}" >> "$dir/nohup.out" 2>&1 &
        pid=$!
        echo "$pid"
        ;;
    *)
        exit 1
        ;;
esac
