import sys
import yaml
import base64
import json

def parse_rpg_map_yaml(file_path):
    """
    Parses an RPG::Map YAML file and extracts event, page, and command data.
    It preserves !binary content as a special dictionary in JSON.

    Args:
        file_path (str): The path to the YAML file.

    Returns:
        dict: A dictionary containing the parsed map data.
    """
    with open(file_path, 'r', encoding='utf-8') as file:
        class RubyObjectLoader(yaml.SafeLoader):
            pass

        # Constructor for general !ruby/object: tags
        def ruby_object_constructor(loader, tag_suffix, node):
            if isinstance(node, yaml.MappingNode):
                return loader.construct_mapping(node)
            elif isinstance(node, yaml.SequenceNode):
                return loader.construct_sequence(node)
            else:
                return loader.construct_scalar(node)

        # Apply the multi-constructor for any !ruby/object: tag
        RubyObjectLoader.add_multi_constructor('!ruby/object:', ruby_object_constructor)
        
        # Custom constructor for !binary tag: store the raw base64 string
        def binary_constructor(loader, node):
            base64_string = loader.construct_scalar(node)
            return {'__binary_content__': base64_string}

        RubyObjectLoader.add_constructor('!binary', binary_constructor)

        data = yaml.load(file, Loader=RubyObjectLoader)

    final_json_output = {
        "map_data": {
            "tileset_id": data.get('tileset_id', 0),
            "width": data.get('width', 0),
            "height": data.get('height', 0)
        },
        "autoplay_bgm": data.get('autoplay_bgm', False),
        "bgm": data.get('bgm', {'name': '', 'volume': 100, 'pitch': 100}),
        "autoplay_bgs": data.get('autoplay_bgs', False),
        "bgs": data.get('bgs', {'name': '', 'volume': 100, 'pitch': 100}),
        "encounter_list": data.get('encounter_list', []),
        "encounter_step": data.get('encounter_step', 30),
        "events": []
    }

    # Grid data extraction
    grid_data = data.get('data', {}).get('data', '')
    if grid_data:
        lines = grid_data.strip().split('\n')
        if lines:
            first_line = lines[0].split()
            if len(first_line) == 4 and first_line[0] == 'init':
                width = int(first_line[1])
                height = int(first_line[2])
                layers = int(first_line[3])
                grids = {str(z): [] for z in range(layers)}
                
                current_layer = -1
                for line in lines[1:]: # Skip 'init' line
                    line = line.strip()
                    if line.startswith('z = '):
                        current_layer = int(line.split('=')[1].strip())
                    elif current_layer != -1 and line:
                        # Handle both space-separated and list-like strings
                        if '[' in line and ']' in line:
                            # It's a list string like "[1, 2, 3]"
                            try:
                                # Convert to a proper list of integers
                                row_tiles = json.loads(line.replace(' ', ''))
                            except json.JSONDecodeError:
                                # Fallback if json.loads fails, treat as space-separated
                                row_tiles = list(map(int, line.split()))
                        else:
                            # It's a space-separated string
                            row_tiles = list(map(int, line.split()))
                        grids[str(current_layer)].append(row_tiles)

                final_json_output['map_data']['grid_info'] = {
                    "width": width,
                    "height": height,
                    "layers": layers,
                    "grids": grids
                }
    
    # Event data extraction
    for event_id, event_data in data.get('events', {}).items():
        parsed_event = {
            "id": event_id,
            "name": event_data.get('name', ''),
            "x": event_data.get('x', 0),
            "y": event_data.get('y', 0),
            "pages": []
        }
        # Extract other direct attributes of the event
        for key, value in event_data.items():
            if key not in ['id', 'name', 'x', 'y', 'pages']:
                parsed_event[key] = value

        for page_index, page_data in enumerate(event_data.get('pages', [])):
            parsed_page = {
                "page_index": page_index
            }
            # Add all attributes of the page
            for key, value in page_data.items():
                if key not in ['list']: # 'list' is handled separately as 'commands'
                    parsed_page[key] = value
            
            commands = []
            for cmd in page_data.get('list', []):
                command_info = {
                    "code": cmd.get('code', 0),
                    "indent": cmd.get('indent', 0),
                    "parameters": []
                }
                
                # Handle 'parameters' which might contain binary data or complex lists
                parameters = cmd.get('parameters')
                if parameters is not None:
                    # Special handling for parameter lists that might contain !binary
                    processed_params = []
                    for param_item in parameters:
                        if isinstance(param_item, dict) and '__binary_content__' in param_item:
                            # If it's a binary content dict, keep it as is
                            processed_params.append(param_item)
                        elif isinstance(param_item, list):
                            # Recursively process lists within parameters
                            processed_list_item = []
                            for sub_item in param_item:
                                if isinstance(sub_item, dict) and '__binary_content__' in sub_item:
                                    processed_list_item.append(sub_item)
                                else:
                                    processed_list_item.append(sub_item)
                            processed_params.append(processed_list_item)
                        else:
                            processed_params.append(param_item)
                    command_info['parameters'] = processed_params
                else:
                    command_info['parameters'] = [] # Ensure parameters is always a list

                commands.append(command_info)
            
            parsed_page['commands'] = commands
            parsed_event['pages'].append(parsed_page)
        final_json_output['events'].append(parsed_event)

    return final_json_output

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python yml_to_json.py <input_yaml_path> <output_json_path>")
        sys.exit(1)

    input_yaml_file = sys.argv[1]
    output_json_file = sys.argv[2]

    try:
        json_data = parse_rpg_map_yaml(input_yaml_file)
        with open(output_json_file, 'w', encoding='utf-8') as f:
            json.dump(json_data, f, indent=2, ensure_ascii=False)
        print(f"Successfully converted {input_yaml_file} to {output_json_file}")
    except FileNotFoundError:
        print(f"Error: Input YAML file not found at {input_yaml_file}")
        sys.exit(1)
    except yaml.YAMLError as e:
        print(f"Error: Could not parse YAML from {input_yaml_file}. Details: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"An unexpected error occurred: {e}")
        sys.exit(1)