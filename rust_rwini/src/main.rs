use std::env;
use std::io::{self, Lines, BufRead, Write, BufWriter, BufReader, Error, ErrorKind};
use std::path::PathBuf;
use std::fs::File;
use fxhash::{FxBuildHasher, FxHashMap};
use linked_hash_map::LinkedHashMap;
pub type FxLinkedHashMap<K, V> = LinkedHashMap<K, V, FxBuildHasher>;
const VER: f32 = 0.1;
const VERINT: u32 = 20241204;
#[macro_export]
macro_rules! some_err {
    ($result:expr) => {
        match $result {
            Ok(_)=>{},
            Err(e) =>return Some(e),
        }
    };
}
macro_rules! get_some_err {
    ($result:expr) => {
        match $result {
            Ok(v)=>{v},
            Err(e) =>return Some(e),
        }
    };
}
fn read_cnf_file(
    srcf: &PathBuf,
) -> Result<FxLinkedHashMap<String, FxHashMap<String, String>>, Error> {
    let file = File::open(srcf)?;
    read_cnf(&mut BufReader::new(file).lines())
}
fn read_cnf<I: BufRead>(
    lte: &mut Lines<I>,
) -> Result<FxLinkedHashMap<String, FxHashMap<String, String>>, Error> {
    let mut table: FxLinkedHashMap<String, FxHashMap<String, String>> = FxLinkedHashMap::default();
    let mut strbuf: String = String::new();
    let mut lastkey: Option<String> = None;
    let mut last: Option<&mut FxHashMap<String, String>> = None;
    'wh: while let Some(line) = lte.next() {
        let mut linev = line?;
        let mut var = linev.trim();
        if var.len() == 0 {
            continue;
        }
        let c: u8 = var.as_bytes()[0];
        if c == b'#' {
            continue;
        }
        let skip = c == b'"';
        let mut ispix = false;
        strbuf.clear();
        loop {
            let mut j: usize = 0;
            while j < usize::MAX {
                let (k, m) = match var[j..].find("\"\"\"") {
                    Some(i) => {
                        ispix = !ispix;
                        (i, i + 3)
                    }
                    None => (var.len(), usize::MAX),
                };
                if !skip {
                    strbuf.push_str(&var[j..k]);
                }
                j = m;
            }
            if ispix {
                match lte.next() {
                    Some(str) => {
                        linev = str?;
                        var = linev.trim();
                    }
                    None => return Err(io::Error::new(ErrorKind::UnexpectedEof, "")),
                }
            } else {
                if skip {
                    continue 'wh;
                } else {
                    var = &strbuf;
                    break;
                }
            }
        }
        let i: usize = var.len() - 1;
        if var.as_bytes()[0] == b'[' && matches!(var[1..].find(']'),Some(x)if x==i) {
            if var[1..].starts_with("comment_") {
                last = None;
                lastKey = None;
            } else {
                let str = &var[1..i];
                lastkey = Some(str.to_string());
                last = table.get_mut(str);
            }
        } else if lastkey.is_some() || last.is_some() {
            let list: Vec<&str> = var.splitn(2, |c| c == ':' || c == '=');
            if list.len() > 1 {
                if last.is_none() {
                    if let Some(key) = lastkey.take() {
                        last = Some(table.entry(key).or_insert(FxHashMap::default()));
                    }
                }
                if let Some(ref mut map) = last {
                    map.insert(list[0].trim().to_string(), list[1].trim().to_string());
                }
            }
        }
    }
    Ok(table)
}
fn write_cnf_file(
    drcf: &PathBuf,
    map: &FxLinkedHashMap<String, FxHashMap<String, String>>,
) -> Option<Error> {
    let f = get_some_err!(File::open(drcf));
    write_cnf_io(&mut BufWriter::new(f), map)
}
fn write_cnf_io<W: Write>(
    buf: &mut BufWriter<W>,
    map: &FxLinkedHashMap<String, FxHashMap<String, String>>,
) -> Option<Error> {
    let mut ishead = false;
    for (k, v) in map {
        if v.len() == 0 {
            continue;
        }
        if ishead {
            some_err!(buf.write(&[b'\n']));
        }
        ishead = true;
        some_err!(buf.write(&[b'[']));
        some_err!(buf.write(k.as_bytes()));
        some_err!(buf.write(&[b']']));
        for (k2, v2) in v {
            some_err!(buf.write(&[b'\n']));
            some_err!(buf.write(k2.as_bytes()));
            some_err!(buf.write(&[b':']));
            some_err!(buf.write(v2.as_bytes()));
        }
    }
    None
}
fn main() {
    let args: Vec<String> = env::args().collect();
    let len = args.len();
    if len > 1 {
        match &args[1][..] {
            "v" => {
                println!("v{}({})", VER, VERINT);
            }
            "f" => {
                if len < 3 {
                    println!("No path");
                } else {
                    match env::current_dir() {
                        Ok(mut path) => {
                            path.push(&args[2][..]);
                            if path.exists() {
                                read_cnf_file(&path);
                            } else {
                                println!("File does not exist");
                            }
                        }
                        Err(err) => {
                            eprintln!("{:?}", err);
                        }
                    }
                }
            }
            _ => {
                println!("Unknown Command");
            }
        }
    }
}
