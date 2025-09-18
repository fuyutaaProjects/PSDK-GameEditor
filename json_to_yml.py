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
    
    # Dictionnaire pour stocker les références des MoveCommands
    move_command_references = {}
    move_command_counter = 1
    
    for event_id in sorted(events_dict_for_yaml.keys()): 
        event_data = events_dict_for_yaml[event_id]
        yaml_lines.append(f"  {event_id}: !ruby/object:RPG::Event") 
        
        event_attrs_to_dump = {k: v for k, v in event_data.items() if k not in ['pages', 'id', 'name', 'x', 'y']} 
        
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
            # CORRECTION PRINCIPALE : Normaliser la structure des commandes
            commands = []
            page_data_copy = page_data.copy()  # Copie pour éviter de modifier l'original
            
            # Gérer les deux cas : 'commands' ou 'list'
            if 'commands' in page_data_copy: 
                commands = page_data_copy.pop('commands', []) 
            elif 'list' in page_data_copy:
                commands = page_data_copy.pop('list', [])

            yaml_lines.append(f"    - !ruby/object:RPG::Event::Page") 
            
            # Maintenir l'ordre original des propriétés
            ordered_props = ['through', 'move_frequency', 'move_type', 'trigger', 'always_on_top', 'walk_anime', 'move_speed', 'step_anime', 'direction_fix']
            
            for prop in ordered_props:
                if prop in page_data_copy:
                    value = page_data_copy[prop]
                    if isinstance(value, bool):
                        yaml_lines.append(f"      {prop}: {str(value).lower()}")
                    else:
                        yaml_lines.append(f"      {prop}: {value}")

            if 'graphic' in page_data_copy and page_data_copy['graphic']:
                yaml_lines.append(f"      graphic: !ruby/object:RPG::Event::Page::Graphic") 
                graphic_content_yaml = yaml.dump(page_data_copy['graphic'],
                                                  Dumper=CustomDumper,
                                                  default_flow_style=False,
                                                  sort_keys=False,
                                                  indent=2,
                                                  width=float('inf')) 
                for line in graphic_content_yaml.splitlines():
                    if line.strip():
                        yaml_lines.append(f"        {line}") 
            
            if 'condition' in page_data_copy and page_data_copy['condition']:
                yaml_lines.append(f"      condition: !ruby/object:RPG::Event::Page::Condition") 
                condition_content_yaml = yaml.dump(page_data_copy['condition'],
                                                    Dumper=CustomDumper,
                                                    default_flow_style=False,
                                                    sort_keys=False,
                                                    indent=2,
                                                    width=float('inf')) 
                for line in condition_content_yaml.splitlines():
                    if line.strip():
                        yaml_lines.append(f"        {line}") 

            if 'move_route' in page_data_copy and page_data_copy['move_route']:
                yaml_lines.append(f"      move_route: !ruby/object:RPG::MoveRoute") 
                move_route_content_yaml = yaml.dump(page_data_copy['move_route'],
                                                     Dumper=CustomDumper,
                                                     default_flow_style=False,
                                                     sort_keys=False,
                                                     indent=2,
                                                     width=float('inf')) 
                for line in move_route_content_yaml.splitlines():
                    if line.strip():
                        yaml_lines.append(f"        {line}") 
            
            # SECTION CORRIGÉE : Traitement des commandes d'événement avec gestion des Set Move Route
            if commands:
                yaml_lines.append("      list:") 
                
                for cmd_index, command in enumerate(commands):
                    # Créer une copie pour éviter de modifier l'original
                    command_to_dump = command.copy()
                    
                    # Traiter les paramètres (notamment les données binaires)
                    if 'parameters' in command_to_dump and isinstance(command_to_dump['parameters'], list):
                        processed_params = []
                        for p in command_to_dump['parameters']:
                            processed_p = process_parameters_recursively(p)
                            processed_params.append(processed_p)
                        command_to_dump['parameters'] = processed_params
                    
                    # CORRECTION CRITIQUE : Convertir les valeurs numériques en entiers
                    if 'code' in command_to_dump:
                        if isinstance(command_to_dump['code'], str):
                            command_to_dump['code'] = int(command_to_dump['code'])
                    if 'indent' in command_to_dump:
                        if isinstance(command_to_dump['indent'], str):
                            command_to_dump['indent'] = int(command_to_dump['indent'])
                    
                    # Gestion spéciale pour les commandes "Set Move Route" (code 209)
                    if command_to_dump.get('code') == 209:
                        yaml_lines.append(f"      - !ruby/object:RPG::EventCommand")
                        
                        # Les paramètres du Set Move Route
                        params = command_to_dump.get('parameters', [])
                        if len(params) >= 2 and isinstance(params[1], dict):
                            move_route_data = params[1]
                            
                            # Écrire les paramètres
                            yaml_lines.append(f"        parameters:")
                            yaml_lines.append(f"        - {params[0] if len(params) > 0 else 0}")
                            yaml_lines.append(f"        - !ruby/object:RPG::MoveRoute")
                            
                            # Écrire repeat et skippable
                            yaml_lines.append(f"          repeat: {str(move_route_data.get('repeat', False)).lower()}")
                            yaml_lines.append(f"          skippable: {str(move_route_data.get('skippable', False)).lower()}")
                            yaml_lines.append(f"          list:")
                            
                            # Traiter la liste des MoveCommands
                            move_commands = move_route_data.get('list', [])
                            for move_cmd_index, move_cmd in enumerate(move_commands):
                                # Pour toutes les commandes sauf la dernière, créer une référence
                                if move_cmd_index < len(move_commands) - 1:
                                    ref_id = move_command_counter
                                    move_command_references[f"cmd_{cmd_index}_{move_cmd_index}"] = ref_id
                                    yaml_lines.append(f"          - &{ref_id} !ruby/object:RPG::MoveCommand")
                                    move_command_counter += 1
                                else:
                                    # Dernière commande sans référence
                                    yaml_lines.append(f"          - !ruby/object:RPG::MoveCommand")
                                
                                # Écrire le code et les paramètres de la MoveCommand
                                yaml_lines.append(f"            code: {move_cmd.get('code', 0)}")
                                move_cmd_params = move_cmd.get('parameters', [])
                                if move_cmd_params:
                                    yaml_lines.append(f"            parameters:")
                                    for param in move_cmd_params:
                                        yaml_lines.append(f"            - {param}")
                                else:
                                    yaml_lines.append(f"            parameters: []")
                        
                        # Écrire indent et code
                        yaml_lines.append(f"        indent: {command_to_dump.get('indent', 0)}")
                        yaml_lines.append(f"        code: {command_to_dump.get('code')}")
                    
                    # Gestion spéciale pour les commandes de continuation (code 509)
                    elif command_to_dump.get('code') == 509:
                        yaml_lines.append(f"      - !ruby/object:RPG::EventCommand")
                        
                        # Chercher la référence correspondante
                        ref_key = f"cmd_{cmd_index-1}_0"  # Généralement la première commande du move route précédent
                        if ref_key in move_command_references:
                            ref_id = move_command_references[ref_key]
                            yaml_lines.append(f"        parameters:")
                            yaml_lines.append(f"        - *{ref_id}")
                        else:
                            # Fallback si pas de référence trouvée
                            yaml_lines.append(f"        parameters:")
                            if 'parameters' in command_to_dump and command_to_dump['parameters']:
                                for param in command_to_dump['parameters']:
                                    yaml_lines.append(f"        - {param}")
                            else:
                                yaml_lines.append(f"        - []")
                        
                        yaml_lines.append(f"        indent: {command_to_dump.get('indent', 0)}")
                        yaml_lines.append(f"        code: {command_to_dump.get('code')}")
                    
                    # Traitement normal pour les autres commandes
                    else:
                        yaml_lines.append(f"      - !ruby/object:RPG::EventCommand")
                        
                        # Maintenir l'ordre des propriétés : parameters, indent, code
                        if 'parameters' in command_to_dump:
                            yaml_lines.append(f"        parameters:")
                            if isinstance(command_to_dump['parameters'], list):
                                for param in command_to_dump['parameters']:
                                    if isinstance(param, str):
                                        yaml_lines.append(f"        - {param}")
                                    else:
                                        yaml_lines.append(f"        - {param}")
                            else:
                                yaml_lines.append(f"        - {command_to_dump['parameters']}")
                        
                        yaml_lines.append(f"        indent: {command_to_dump.get('indent', 0)}")
                        yaml_lines.append(f"        code: {command_to_dump.get('code')}")

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