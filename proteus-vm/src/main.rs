// Contains needed traits
extern crate enum_index;
// Contains derives
#[macro_use]
extern crate enum_index_derive;
extern crate strum;
#[macro_use]
extern crate strum_macros;


use std::fs;

use clap::Arg;

use crate::evaluator::Evaluator;

mod instructions;
mod loading;
mod evaluator;
mod preprocessor;
mod utils;
mod memory;
mod ffi;

fn main() {
    let matches = clap::Command::new("Rust VM")
        .version("0.1.0")
        .author("Julian Hartl <")
        .about("A simple virtual machine written in Rust")
        .subcommand(clap::Command::new("run")
            .about("Runs a file")
            .args(vec![
                Arg::new("file")
                    .help("The file to run")
                    .required(true)
                    .index(1),
                Arg::new("step")
                    .help("Step through the execution")
                    .num_args(0)
                    .required(false)
                    .short('s'),
            ]))
        .subcommand(clap::Command::new("transpile")
            .about("Transpiles a file into proteus byte code")
            .args(vec![Arg::new("file")
                           .help("The file to transpile")
                           .required(true)
                           .index(1),
                       Arg::new("output")
                           .help("The output file")
                           .required(false)
                           .short('o'),
            ])
        )
        .get_matches();
    if let Some(matches) = matches.subcommand_matches("run") {
        let file = matches.get_one::<String>("file").unwrap();

        println!("Running file: {}", file);
        let content = fs::read(file).unwrap();
        let mut evaluator = Evaluator::new(&content);
        let step = matches.get_flag("step");
        if step {
            let mut next_breakpoint = Some(1);
            while let Some(instruction) = evaluator.next() {
                println!("Instruction: {:?}", instruction);
                evaluator.evaluate_instruction(&instruction).unwrap();
                if let Some(breakpoint) = next_breakpoint {
                    let instruction_counter = evaluator.byte_code_parser.instruction_counter;
                    if instruction_counter == breakpoint {
                        next_breakpoint = Some(breakpoint + 1);
                    }
                } else {
                    continue;
                }
                evaluator.print_state();
                let mut matches_command = true;
                while matches_command {
                    let mut command = String::new();
                    std::io::stdin().read_line(&mut command).unwrap();
                    matches_command = match &command {
                        command if command.starts_with("#h") => {
                            let split: Vec<&str> = command.split(" ").map(|s| s.trim()).collect();
                            let address = split[1].parse::<usize>().unwrap();
                            let bytes = split.get(2).map(|x| x.parse::<u8>().unwrap()).unwrap_or(4);
                            let heap = &evaluator.memory.heap;
                            let bytes = heap.load(address, bytes as usize).unwrap();
                            println!("Heap [{}]: {:?}", address, bytes);
                            true
                        }

                        command if command.starts_with("#b") => {
                            let split: Vec<&str> = command.split(" ").map(|s| s.trim()).collect();
                            let instruction = split[1].parse::<usize>().unwrap();
                            next_breakpoint = Some(instruction);
                            true
                        }

                        _ => {
                            false
                        }
                    }
                }
            }
        } else {
            let now = std::time::Instant::now();
            println!();
            evaluator.evaluate().unwrap();
            println!();

            println!("Execution time: {}ms", now.elapsed().as_millis());

        }
    }

    if let Some(matches) = matches.subcommand_matches("transpile") {
        let file = matches.get_one::<String>("file").unwrap();
        println!("Transpiling file: {}", file);
        let code = fs::read_to_string(file).unwrap();
        let (transpiled, symbol_table) = preprocessor::process(&code);
        let translator = loading::ByteCodeTranslator::new(transpiled.as_str(), &symbol_table);
        let byte_code = translator.translate();
        println!("Byte code: {:?}", byte_code);
        let default_output = "out.proteus".to_string();
        let output_file = matches.get_one::<String>("output").unwrap_or(&default_output);
        fs::write(output_file, byte_code).unwrap();
        println!("Wrote byte code to file: {}", output_file);
    }
}





