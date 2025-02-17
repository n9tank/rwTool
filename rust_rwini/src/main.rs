use std::env;
use std::ptr::slice_from_raw_parts;
use std::rc::Rc;
use std::io::{self, Lines, BufRead, Write, BufWriter, BufReader, Error, ErrorKind};
use std::path::PathBuf;
use std::fs::File;
use std::ptr;
use std::sync::LazyLock;
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
pub struct Section(SECKV, Option<SECKV>);
impl Section {
    fn clone(&self) -> Section {
        Section((&self.0).clone(), None)
    }
}
pub type SECKV = FxHashMap<Rc<Box<str>>, Rc<Box<str>>>;
pub type INIKV = FxLinkedHashMap<Rc<Box<str>>, Section>;
pub fn put_INIKV(src: &mut INIKV, drc: &INIKV) {
    let STR_COPYSKIP: Box<str> = "@copyFrom_skipThisSection".into();
    for (k, v) in drc.iter() {
        if matches!((v.0).get(&STR_COPYSKIP),
        Some(x)if (***x).eq("1") || x.eq_ignore_ascii_case("true"))
        {
            let sec = src.get_mut(k);
            if let Some(secv) = sec {
                for (k0, v0) in v.0.iter() {
                    secv.0.insert(k0.clone(), v0.clone());
                }
            } else {
                src.insert(k.clone(), v.clone());
            }
        }
    }
}
pub fn read_cnf_file(srcf: &PathBuf) -> Result<INIKV, Error> {
    let file = File::open(srcf)?;
    read_cnf(&mut BufReader::new(file).lines())
}
pub fn read_cnf<I: BufRead>(lte: &mut Lines<I>) -> Result<INIKV, Error> {
    let mut table: INIKV = FxLinkedHashMap::default();
    let mut strbuf: String = String::new();
    let mut lastkey: Option<Box<str>> = None;
    let mut last: Option<&mut SECKV> = None;
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
                lastkey = None;
            } else {
                let str = Box::from(&var[1..i]);
                if let Some(v) = table.get_mut(&str) {
                    last = Some(&mut v.0);
                } else {
                    last = None;
                }
                lastkey = Some(str);
            }
        } else if last.is_some() || lastkey.is_some() {
            let mut list = var.splitn(2, |c| c == ':' || c == '=');
            if let Some(key) = list.next() {
                if let Some(value) = list.next() {
                    if last.is_none() {
                        if let Some(key) = lastkey.take() {
                            last = Some(
                                &mut table
                                    .entry(Rc::new(key))
                                    .or_insert(Section(FxHashMap::default(), None))
                                    .0,
                            );
                        }
                    }
                    if let Some(ref mut map) = last {
                        map.insert(
                            Rc::new(Box::from(key.trim())),
                            Rc::new(Box::from(value.trim())),
                        );
                    }
                }
            }
        }
    }
    Ok(table)
}
pub fn write_cnf_file(drcf: &PathBuf, map: &INIKV) -> Option<Error> {
    let f = get_some_err!(File::open(drcf));
    write_cnf_io(&mut BufWriter::new(f), map)
}
pub fn write_cnf_io<W: Write>(buf: &mut BufWriter<W>, map: &INIKV) -> Option<Error> {
    let mut ishead = false;
    for (k, vs) in map {
        let v = &vs.0;
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
