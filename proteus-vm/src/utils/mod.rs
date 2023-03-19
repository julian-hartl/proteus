pub fn decode_unsigned(from: usize, byte_code: &[u8]) -> Result<u32, String> {
    if byte_code.len() < from + 4 {
        return Err("Missing bytes while trying to convert to unsigned integer".to_string());
    }
    let x = &byte_code[from..from + 4];
    Ok(u32::from_be_bytes(x.try_into().map_err(|_| "Could not convert to u32".to_string())?))
}

pub fn decode_signed(from: usize, byte_code: &[u8]) -> Result<i32, String> {
    if byte_code.len() < from + 4 {
        return Err("Missing bytes while trying to convert to signed integer".to_string());
    }
    Ok(i32::from_be_bytes(byte_code[from..from + 4].try_into().map_err(|_| "Could not convert to i32".to_string())?))
}

pub fn encode_unsigned(value: u32) -> [u8; 4] {
    value.to_be_bytes()
}

pub fn encode_signed(value: i32) -> [u8; 4] {
    value.to_be_bytes()
}

pub fn encode_string(value: &str) -> Vec<u8> {
    let mut encoded = Vec::new();
    for byte in value.bytes() {
        encoded.push(byte);
    }
    encoded
}

pub fn decode_string(from: u32, bytes: &[u8]) -> String {
    let mut string = String::new();
    let mut index = from as usize;
    while bytes[index] != 0 {
        string.push(bytes[index] as char);
        index += 1;
    }
    string
}

