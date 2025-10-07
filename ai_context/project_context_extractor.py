import os

# Configuration
PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), os.pardir))
OUTPUT_DIR = os.path.join(PROJECT_ROOT, "ai_context")
OUTPUT_FILENAME = "project_context.txt"

# Files and directories to include/exclude
INCLUDE_EXTENSIONS = ['.java', '.xml', '.properties', '.html', '.css', '.js']
EXCLUDE_DIRS = ['target', '.git', '.idea', 'build', 'node_modules', OUTPUT_DIR.split(os.sep)[-1]]
EXCLUDE_FILES = ['project_context_extractor.py']


def generate_tree(root_dir, prefix=""):
    """
    Recursively generates a tree structure string of the project.
    """
    tree_str = ""
    entries = sorted(os.listdir(root_dir))
    entries = [e for e in entries if e not in EXCLUDE_DIRS and e not in EXCLUDE_FILES]

    for i, entry in enumerate(entries):
        path = os.path.join(root_dir, entry)
        connector = "└── " if i == len(entries) - 1 else "├── "
        tree_str += prefix + connector + entry + "\n"

        if os.path.isdir(path):
            extension = "    " if i == len(entries) - 1 else "│   "
            tree_str += generate_tree(path, prefix + extension)

    return tree_str


def extract_project_context():
    """
    Walks through the project directory, extracts content from specified files,
    and compiles it into a single output file along with a project structure.
    """
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    output_filepath = os.path.join(OUTPUT_DIR, OUTPUT_FILENAME)

    with open(output_filepath, 'w', encoding='utf-8') as outfile:
        outfile.write(f"--- Project Context for AI Assistant ---\n\n")
        outfile.write(f"Project Root: {PROJECT_ROOT}\n")
        outfile.write(f"Generated On: {os.path.getmtime(output_filepath) if os.path.exists(output_filepath) else 'N/A'}\n\n")

        # Write project structure (only in file, not printed)
        outfile.write("--- Project Structure ---\n")
        tree_structure = generate_tree(PROJECT_ROOT)
        outfile.write(tree_structure + "\n")

        # Write file contents
        outfile.write("--- Project Files Content ---\n")
        for root, dirs, files in os.walk(PROJECT_ROOT):
            dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]

            for filename in files:
                file_path = os.path.join(root, filename)
                relative_path = os.path.relpath(file_path, PROJECT_ROOT)

                if (any(relative_path.endswith(ext) for ext in INCLUDE_EXTENSIONS) and
                        filename not in EXCLUDE_FILES):
                    try:
                        with open(file_path, 'r', encoding='utf-8') as infile:
                            content = infile.read()
                            outfile.write(f"\n--- FILE: {relative_path} ---\n")
                            outfile.write(content)
                            outfile.write("\n--- END FILE ---\n")
                    except Exception as e:
                        outfile.write(f"\n--- ERROR READING FILE: {relative_path} ---\n")
                        outfile.write(f"Error: {e}\n")
                        outfile.write("\n--- END ERROR ---\n")

        outfile.write(f"\n--- End of Project Context ---\n")

    print(f"Project context extracted successfully to: {output_filepath}")


if __name__ == "__main__":
    extract_project_context()
