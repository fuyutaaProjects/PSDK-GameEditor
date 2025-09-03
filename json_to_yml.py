import yaml
import base64
import json
import sys
import os
import traceback

def reconstruct_rpg_map_yaml(json_data, output_file_path):

    def process_parameters_recursively(param_item):
        if isinstance(param_item, dict) and '__binary_content__' in param_item:
            base64_str = param_item['__binary_content__']
            return base64.b64decode(base64_str.encode('ascii'))
        elif isinstance(param_item, str):
            return param_item
        elif isinstance(param_item, list):
            return [process_parameters_recursively(item) for item in param_item]
        else:
            return param_item

    class CustomDumper(yaml.SafeDumper):
        def represent_tag(self, tag, data, flow_style=None):
            if isinstance(data, dict):
                return self.represent_mapping(tag, data, flow_style=flow_style)
            elif isinstance(data, list):
                return self.represent_sequence(tag, data, flow_style=flow_style)
            else:
                return self.represent_scalar(tag, data)

        def represent_str(self, data):
            if isinstance(data, str) and (
                ':' in data or '[' in data or ']' in data or '{' in data or '}' in data or
                data.startswith(('!', '-', '?', ':', '|', '>')) or
                data.lower() in ('true', 'false', 'y', 'n', 'yes', 'no') or
                data.strip() == ''
            ):
                return self.represent_scalar('tag:yaml.org,2002:str', data, style='"')
            return super().represent_str(data)

        def represent_binary(self, data):
            encoded_data = base64.b64encode(data).decode('ascii')
            return self.represent_scalar('!binary', encoded_data, style='|')

    CustomDumper.add_representer(bytes, CustomDumper.represent_binary)

    def ruby_object_representer(dumper, data):
        if isinstance(data, dict):
            # RPG::AudioFile
            if all(k in data for k in ['name', 'volume', 'pitch']):
                return dumper.represent_mapping('!ruby/object:RPG::AudioFile', data)
            # RPG::MoveCommand
            elif all(k in data for k in ['code', 'parameters']):
                if not any(k in data for k in ['repeat', 'skippable', 'list']):
                    return dumper.represent_mapping('!ruby/object:RPG::MoveCommand', data)
            # RPG::MoveRoute
            elif all(k in data for k in ['repeat', 'skippable', 'list']):
                 return dumper.represent_mapping('!ruby/object:RPG::MoveRoute', data)
            # RPG::Event::Page::Condition
            elif all(k in data for k in ['switch1_valid', 'self_switch_ch', 'switch1_id', 'switch2_valid', 'variable_value', 'self_switch_valid', 'variable_id', 'variable_valid', 'switch2_id']):
                return dumper.represent_mapping('!ruby/object:RPG::Event::Page::Condition', data)
            # RPG::Event::Page::Graphic
            elif all(k in data for k in ['character_name', 'character_index', 'direction', 'pattern', 'opacity', 'blend_type']):
                return dumper.represent_mapping('!ruby/object:RPG::Event::Page::Graphic', data)
            
            # Fallback générique pour les autres dicts qui sont des objets Ruby
            return dumper.represent_mapping('!ruby/object:', data)
        return dumper.represent_data(data)

    CustomDumper.add_multi_representer(dict, ruby_object_representer)

    # Initialisation de yaml_lines AVANT toute utilisation
    yaml_lines = []
    yaml_lines.append("--- !ruby/object:RPG::Map")

    reconstructed_data = {
        'tileset_id': json_data.get('map_data', {}).get('tileset_id', 0),
        'width': json_data.get('map_data', {}).get('width', 0),
        'height': json_data.get('map_data', {}).get('height', 0),
        'autoplay_bgm': json_data.get('autoplay_bgm', False),
        'bgm': json_data.get('bgm', {'name': '', 'volume': 100, 'pitch': 100}),
        'autoplay_bgs': json_data.get('autoplay_bgs', False), # Boolean for autoplay
        'bgs_object': json_data.get('bgs', {'name': '', 'volume': 100, 'pitch': 100}), # The BGS AudioFile object
        'encounter_list': json_data.get('encounter_list', []),
        'encounter_step': json_data.get('encounter_step', 30),
    }

    grid_info_json = json_data.get('map_data', {}).get('grid_info', {}) 
    
    table_data_string = ""
    if grid_info_json:
        width = grid_info_json.get('width', 0)
        height = grid_info_json.get('height', 0)
        layers = grid_info_json.get('layers', 3)
        table_lines = [f"init {width} {height} {layers}"] # Initialiser table_lines ici
        grids = grid_info_json.get('grids', {})

        for z in range(layers):
            table_lines.append(f"z = {z}")
            layer_grid_data = grids.get(str(z), []) 
            
            if isinstance(layer_grid_data, list) and all(isinstance(sublist, list) for sublist in layer_grid_data):
                layer_grid_flat = [item for sublist in layer_grid_data for item in sublist]
            elif isinstance(layer_grid_data, list):
                layer_grid_flat = layer_grid_data 
            else:
                print(f"Warning: Unexpected data type for layer grid {z}. Expected a list or list of lists. Filling with zeros.")
                layer_grid_flat = [0] * (width * height) 

            for y in range(height):
                row_start_index = y * width
                row_end_index = row_start_index + width
                
                if row_end_index <= len(layer_grid_flat):
                    row_tiles = layer_grid_flat[row_start_index : row_end_index]
                    row_str = " ".join(map(str, row_tiles))
                    table_lines.append(row_str) # Ajouter à table_lines
                else:
                    print(f"Warning: Insufficient grid data for layer {z}, row {y}. Padding with zeros.")
                    table_tiles = ["0"] * width
                    table_lines.append(" ".join(table_tiles)) # Ajouter à table_lines
        
        table_data_string = "\n".join(table_lines) # Joindre table_lines pour former table_data_string
    else:
        print("Warning: 'grid_info' not found in JSON, using empty default grid for 'data'.")
        default_width = 20
        default_height = 20
        default_layers = 3
        table_lines = [f"init {default_width} {default_height} {default_layers}"] # Initialiser table_lines ici
        for z in range(default_layers):
            table_lines.append(f"z = {z}")
            for y in range(default_height):
                table_lines.append(("0 " * default_width).strip())
        table_data_string = "\n".join(table_lines).strip() # Joindre table_lines pour former table_data_string


    events_data = json_data.get('events', []) 
    events_dict_for_yaml = {}
    for event_item in events_data: 
        event_id = event_item.get('id')
        if event_id is not None:
            events_dict_for_yaml[event_id] = event_item

    # Cette partie de yaml_lines avait été déplacée, elle est maintenant à la bonne place
    yaml_lines.append(f"tileset_id: {reconstructed_data['tileset_id']}")
    yaml_lines.append(f"width: {reconstructed_data['width']}")
    yaml_lines.append(f"height: {reconstructed_data['height']}")
    yaml_lines.append(f"autoplay_bgm: {str(reconstructed_data['autoplay_bgm']).lower()}")

    bgm_content_yaml = yaml.dump(reconstructed_data['bgm'], 
                                  Dumper=CustomDumper, 
                                  default_flow_style=False, 
                                  sort_keys=False, 
                                  indent=2,
                                  width=float('inf')).strip()
    yaml_lines.append(f"bgm: !ruby/object:RPG::AudioFile")
    for line in bgm_content_yaml.splitlines():
        yaml_lines.append(f"  {line}") 
    
    yaml_lines.append(f"autoplay_bgs: {str(reconstructed_data['autoplay_bgs']).lower()}")
    
    bgs_content_yaml = yaml.dump(reconstructed_data['bgs_object'], 
                                  Dumper=CustomDumper, 
                                  default_flow_style=False, 
                                  sort_keys=False, 
                                  indent=2, 
                                  width=float('inf')).strip()
    yaml_lines.append(f"bgs: !ruby/object:RPG::AudioFile")
    for line in bgs_content_yaml.splitlines():
        yaml_lines.append(f"  {line}")


    yaml_lines.append(f"encounter_list: {reconstructed_data['encounter_list']}") 
    yaml_lines.append(f"encounter_step: {reconstructed_data['encounter_step']}")

    yaml_lines.append("data: !ruby/object:Table") 
    yaml_lines.append("  data: |") 
    for line in table_data_string.splitlines():
        yaml_lines.append(f"    {line}") 


    yaml_lines.append("events:") 
    
    for event_id in sorted(events_dict_for_yaml.keys()): 
        event_data = events_dict_for_yaml[event_id]
        yaml_lines.append(f"  {event_id}: !ruby/object:RPG::Event") 
        
        event_attrs_to_dump = {k: v for k, v in event_data.items() if k not in ['pages', 'id', 'name', 'x', 'y']} 
        
        if not event_attrs_to_dump:
            print(f"DEBUG: Event {event_id} has no filtered attributes for the main RPG::Event object.")

        event_attrs_yaml = yaml.dump(event_attrs_to_dump,
                                      Dumper=CustomDumper,
                                      default_flow_style=False,
                                      sort_keys=False,
                                      indent=2,
                                      width=float('inf')) 
        
        if event_attrs_yaml.strip() != '{}' and event_attrs_yaml.strip() != '':
            for line in event_attrs_yaml.splitlines():
                if line.strip():
                    yaml_lines.append(f"    {line}") 

        yaml_lines.append(f"    id: {event_data.get('id', 0)}")
        yaml_lines.append(f"    name: {event_data.get('name', '')}")
        yaml_lines.append(f"    x: {event_data.get('x', 0)}")
        yaml_lines.append(f"    y: {event_data.get('y', 0)}")


        yaml_lines.append("    pages:") 
        for page_index, page_data in enumerate(event_data.get('pages', [])):
            commands = [] 

            yaml_lines.append(f"    - !ruby/object:RPG::Event::Page") 
            
            if 'commands' in page_data: 
                commands = page_data.pop('commands', []) 
            
            simple_page_attrs = {
                k: v for k, v in page_data.items() 
                if k not in ['graphic', 'condition', 'move_route', 'page_index'] 
            }
            
            simple_attrs_yaml = yaml.dump(simple_page_attrs,
                                          Dumper=CustomDumper,
                                          default_flow_style=False,
                                          sort_keys=False,
                                          indent=2,
                                          width=float('inf')) 
            for line in simple_attrs_yaml.splitlines():
                if line.strip():
                    yaml_lines.append(f"      {line}") 
                else:
                    yaml_lines.append("") 

            if 'graphic' in page_data and page_data['graphic']:
                yaml_lines.append(f"      graphic: !ruby/object:RPG::Event::Page::Graphic") 
                graphic_content_yaml = yaml.dump(page_data['graphic'],
                                                  Dumper=CustomDumper,
                                                  default_flow_style=False,
                                                  sort_keys=False,
                                                  indent=2,
                                                  width=float('inf')) 
                for line in graphic_content_yaml.splitlines():
                    if line.strip():
                        yaml_lines.append(f"        {line}") 
                    else:
                        yaml_lines.append("") 
            
            if 'condition' in page_data and page_data['condition']:
                yaml_lines.append(f"      condition: !ruby/object:RPG::Event::Page::Condition") 
                condition_content_yaml = yaml.dump(page_data['condition'],
                                                    Dumper=CustomDumper,
                                                    default_flow_style=False,
                                                    sort_keys=False,
                                                    indent=2,
                                                    width=float('inf')) 
                for line in condition_content_yaml.splitlines():
                    if line.strip():
                        yaml_lines.append(f"        {line}") 
                    else:
                        yaml_lines.append("") 

            print(f"DEBUG: Page {page_index} (Event {event_id}) move_route data before processing: {page_data.get('move_route', 'NOT FOUND')}")
            if 'move_route' in page_data and page_data['move_route']:
                yaml_lines.append(f"      move_route: !ruby/object:RPG::MoveRoute") 
                move_route_content_yaml = yaml.dump(page_data['move_route'],
                                                     Dumper=CustomDumper,
                                                     default_flow_style=False,
                                                     sort_keys=False,
                                                     indent=2,
                                                     width=float('inf')) 
                print(f"DEBUG: Page {page_index} (Event {event_id}) move_route YAML content after dump: \n{move_route_content_yaml}")
                for line in move_route_content_yaml.splitlines():
                    if line.strip():
                        yaml_lines.append(f"        {line}") 
                    else:
                        yaml_lines.append("") 
            
            # SECTION CORRIGÉE : Traitement des commandes d'événement
            if commands:
                yaml_lines.append("      list:") 
                
                for command in commands:
                    # Créer une copie pour éviter de modifier l'original
                    command_to_dump = command.copy()
                    
                    # Traiter les paramètres (notamment les données binaires)
                    if 'parameters' in command_to_dump and isinstance(command_to_dump['parameters'], list):
                        processed_params = []
                        for p in command_to_dump['parameters']:
                            processed_p = process_parameters_recursively(p)
                            processed_params.append(processed_p)
                        command_to_dump['parameters'] = processed_params
                    
                    # Convertir les valeurs numériques en entiers (pas en strings)
                    if 'code' in command_to_dump:
                        command_to_dump['code'] = int(command_to_dump['code'])
                    if 'indent' in command_to_dump:
                        command_to_dump['indent'] = int(command_to_dump['indent'])
                    
                    # Ajouter le tag Ruby pour RPG::EventCommand
                    yaml_lines.append(f"      - !ruby/object:RPG::EventCommand")
                    
                    # Ajouter command_index s'il n'existe pas (valeur par défaut 0)
                    if 'command_index' not in command_to_dump:
                        command_to_dump['command_index'] = 0
                    
                    # Générer le YAML pour cette commande
                    cmd_attrs_yaml = yaml.dump(command_to_dump,
                                                 Dumper=CustomDumper,
                                                 default_flow_style=False,
                                                 sort_keys=False,
                                                 indent=2,
                                                 width=float('inf')) 
                    
                    # Ajouter chaque ligne avec l'indentation appropriée
                    for line in cmd_attrs_yaml.splitlines():
                        if line.strip():
                            yaml_lines.append(f"        {line}") 

    final_yaml_string = "\n".join(yaml_lines) 

    with open(output_file_path, 'w', encoding='utf-8') as f:
        f.write(final_yaml_string) 

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python json_to_yml.py <input_json_path> <output_yml_path>") 
        sys.exit(1)

    input_file_path = sys.argv[1]
    full_output_path_from_java = sys.argv[2] 

    file_name = os.path.basename(full_output_path_from_java)
    output_file_path = os.path.join(os.path.dirname(sys.argv[0]), "resultat_" + file_name)

    try:
        with open(input_file_path, 'r', encoding='utf-8') as file:
            json_data = json.load(file)
        
        reconstruct_rpg_map_yaml(json_data, output_file_path)
        print(f"Successfully converted {input_file_path} to {output_file_path}") 
    except FileNotFoundError:
        print(f"Error: Input file not found at {input_file_path}") 
        sys.exit(1)
    except json.JSONDecodeError:
        print(f"Error: Could not decode JSON from {input_file_path}. Is it a valid JSON file?") 
        sys.exit(1)
    except Exception as e:
        print(f"An unexpected error occurred:") 
        traceback.print_exc() 
        sys.exit(1)