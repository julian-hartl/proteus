pub struct FFIFunction {
    pub name: String,
    pub arguments: Vec<FFIType>,
    pub return_type: FFIType,
}


impl FFIFunction {
    pub fn new(name: &str, arguments: Vec<FFIType>, return_type: FFIType) -> Self {
        Self {
            name: name.to_string(),
            arguments,
            return_type,
        }
    }

    pub fn println() -> Self {
        Self::new("println", vec![FFIType::String], FFIType::Void)
    }

    pub fn find(index: &usize) -> Option<Self> {
        match index {
            0 => Some(Self::println()),
            _ => None,
        }
    }

    pub fn get_index(name: &str) -> Option<usize> {
        match name {
            "println" => Some(0),
            _ => None,
        }
    }

    pub fn call(&self, arguments: Vec<FFIValue>) -> Result<FFIValue,String> {
        match self.name.as_str() {
            "println" => {
                let output = arguments.get(0).ok_or("Missing argument")?;
                let output = match output {
                    FFIValue::String(string) => string,
                    _ => return Err("Invalid argument".to_string()),
                };
                println!("{}", output);
                Ok(FFIValue::Void)
            }
            _ => Err(format!("Unknown FFI function: {}", self.name)),
        }
    }
}

pub enum FFIType {
    I32,
    I64,
    String,
    Void,
}

#[derive(Debug)]
pub enum FFIValue {
    I32(i32),
    I64(i64),
    String(String),
    Void,
}

