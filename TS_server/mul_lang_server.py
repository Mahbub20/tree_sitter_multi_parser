from flask import Flask, request, jsonify
from tree_sitter import Language, Parser
import tree_sitter_python
import tree_sitter_java

app = Flask(__name__)

# Map languages to grammar bindings
LANGUAGES = {
    'python': Language(tree_sitter_python.language()),
    'java': Language(tree_sitter_java.language()),
}

@app.route('/parse', methods=['POST'])
def parse():
    data = request.get_json()

    if not data:
        return jsonify({'error': 'Missing JSON body'}), 400

    code = data.get('code', '').strip()
    lang = data.get('language', '').strip().lower()

    if not code:
        return jsonify({'error': 'Code is empty'}), 400

    language = LANGUAGES.get(lang)
    if not language:
        return jsonify({'error': f'Language not supported: {lang}'}), 400

    try:
        parser = Parser(language)
        tree = parser.parse(code.encode('utf8'))

        def node_to_dict(node):
            return {
                'type': node.type,
                'start': node.start_point,
                'end': node.end_point,
                'children': [node_to_dict(child) for child in node.children]
            }

        return jsonify(node_to_dict(tree.root_node))

    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5005)

