mod rcstr;
use std::env;
use std::hash::Hash;
use rcstr::Rcstr;
use std::rc::Rc;
use std::io::{self, Read, BufRead, Write, BufWriter, BufReader, Error, ErrorKind};
use std::path::PathBuf;
use std::fs::File;
use std::sync::LazyLock;
use std::borrow::Borrow;
use fxhash::{FxBuildHasher, FxHashMap, FxHashSet};
use linked_hash_map::LinkedHashMap;
pub type FxLinkedHashMap<K, V> = LinkedHashMap<K, V, FxBuildHasher>;
const VER: f32 = 0.1;
const VERINT: u32 = 20241204;
pub struct Section(SECKV, Option<SECKV>, bool);
impl Section {
    fn clone(&self) -> Section {
        Section((&self.0).clone(), None, false)
    }
}
impl Borrow<SECKV> for Section {
    fn borrow(&self) -> &SECKV {
        &self.0
    }
}
pub static MATHEXP: LazyLock<FxHashSet<&'static str>> = LazyLock::new(|| {
    let mut set = FxHashSet::with_capacity_and_hasher(8, FxBuildHasher::default());
    set.insert("sin");
    set.insert("cos");
    set.insert("int");
    set.insert("sqrt");
    set
});
pub type SECKV = FxHashMap<Rcstr, Rcstr>;
pub type INIKV = FxLinkedHashMap<Rcstr, Section>;
pub type SECCF<'a> = FxHashMap<&'a str, &'a str>;
pub type INICF<'a> = FxLinkedHashMap<&'a str, FxHashMap<&'a str, &'a str>>;
pub struct INI<'a> {
    put: INIKV,
    cnf: Option<INICF<'a>>,
    global: SECCF<'a>,
    isini: bool,
    str_list: Vec<Box<str>>,
}
impl<'a> INI<'a> {
    pub fn new(src: INIKV) -> INI<'a> {
        INI {
            put: src,
            cnf: None,
            global: FxHashMap::default(),
            isini: true,
            str_list: Vec::new(),
        }
    }
    pub fn set_cnf(&'a mut self) {
        let mut table: INICF<'a> = FxLinkedHashMap::with_capacity_and_hasher(
            (&self.put).capacity(),
            FxBuildHasher::default(),
        );
        for (k, v) in &self.put {
            let mut table_lv: SECCF<'a> =
                FxHashMap::with_capacity_and_hasher((&v.0).capacity(), FxBuildHasher::default());
            for (k2, v2) in &v.0 {
                table_lv.insert(k2, v2);
            }
            table.insert(k, table_lv);
        }
        self.cnf = Some(table);
    }
    pub fn init_global(&'a mut self) {
        let table: &mut SECCF<'a> = &mut self.global;
        for v in (&self.put).values() {
            for (k2, v2) in &v.0 {
                if k2.starts_with("@global ") {
                    table.insert(&k2[8..], v2);
                }
            }
        }
    }
    pub fn get_define(
        &self,
        var: &'a str,
        thisk: &str,
        thissec: &FxHashMap<Rcstr, Rcstr>,
        buf: &'a mut String,
    ) -> Option<&'a str> {
        buf.clear();
        let mut st: usize = 0;
        while st < var.len() {
            if let Some(start) = var[st..].find("${") {
                let start = start + st;
                if let Some(end) = find_str(var, '}', start + 2) {
                    let end = end + (start + 2);
                    let varm = var[start..end].trim();
                    let mut x = 0;
                    let mut y = 0;
                    while x < varm.len() {
                        if let Some(i) = find_define_start(&varm[x..]) {
                            let i = x + i;
                            buf.push_str(&varm[y..i]);
                            let n = match find_define_end(varm, i + 1) {
                                Some(sn) => sn + i + 1,
                                None => varm.len(),
                            };
                            let rv = &varm[i..n];
                            if MATHEXP.get(rv).is_none() {
                                let vput = {
                                    let mut skv = rv.splitn(2, |x| x == '.');
                                    if let Some(vk) = skv.next() {
                                        if let Some(vv) = skv.next() {
                                            let seckv = match &vk {
                                                &"section" => thissec,
                                                val if val.eq(&thisk) => thissec,
                                                _ => {
                                                    if let Some(getv) = (&self.put).get(vk) {
                                                        &getv.0
                                                    } else {
                                                        return None;
                                                    }
                                                }
                                            };
                                            match seckv.get(vv) {
                                                Some(vr) => vr,
                                                None => return None,
                                            }
                                        } else {
                                            match thissec.get(
                                                format!("{}{}", "@define ", vk).as_str(),
                                            ) {
                                                Some(vr) => vr,
                                                None => {
                                                    if let Some(v) = (&self.global).get(rv) {
                                                        *v
                                                    } else if let Some(v) = thissec.get(rv) {
                                                        v
                                                    } else {
                                                        return None;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        return None;
                                    }
                                };
                                buf.push_str(vput);
                                y = n;
                            } else {
                                y = i;
                            }
                            x = n;
                        } else {
                            break;
                        }
                    }
                    buf.push_str(&varm[y..]);
                    st = end;
                } else {
                    break;
                }
            }
        }
        if st == 0 {
            return Some(&var);
        }
        buf.push_str(&var[st..]);
        Some(&buf.as_str())
    }
}
pub fn find_str(src: &str, c: char, i: usize) -> Option<usize> {
    if i < src.len() {
        return src[i..].chars().position(|x| x == c);
    }
    None
}
pub fn find_define_start(var: &str) -> Option<usize> {
    var.chars().position(|x| {
        x == '_' || (x >= 'a' && x <= 'z') || (x >= 'A' && x <= 'Z')
    })
}
pub fn find_define_end(src: &str, i: usize) -> Option<usize> {
    if i < src.len() {
        return src.chars().position(|x: char| {
            x != '.' && x != '_' && (x < '0' || x > '9') && (x < 'a' || x > 'z') &&
                (x < 'A' || x > 'Z')
        });
    }
    None
}
pub fn put_inikv(src: &mut INIKV, drc: &INIKV) {
    for (k, v) in drc {
        if !matches!((v.0).get("@copyFrom_skipThisSection"),
        Some(x)if (**x).eq("1") || x.eq_ignore_ascii_case("true"))
        {
            let sec = src.get_mut(k);
            if let Some(secv) = sec {
                for (k0, v0) in &v.0 {
                    secv.0.entry(k0.clone()).or_insert_with(|| v0.clone());
                }
            } else {
                src.insert(k.clone(), v.clone());
            }
        }
    }
}
pub fn read_cnf_file(srcf: &PathBuf) -> Result<INIKV, Error> {
    let file = File::open(srcf)?;
    read_cnf(&mut BufReader::new(file))
}
pub fn read_cnf<R: Read>(lte: &mut BufReader<R>) -> Result<INIKV, Error> {
    let mut table: INIKV = FxLinkedHashMap::default();
    let mut strbuf: String = String::new();
    let mut lastkey: Option<Box<str>> = None;
    let mut last: Option<&mut SECKV> = None;
    let mut linev = String::new();
    'wh: loop {
        linev.clear();
        if lte.read_line(&mut linev)? == 0 {
            break;
        }
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
            while j < var.len() {
                let (k, m) = match var[j..].find("\"\"\"") {
                    Some(i) => {
                        ispix = !ispix;
                        (i + j, true)
                    }
                    None => (var.len(), false),
                };
                if !skip {
                    strbuf.push_str(&var[j..k]);
                }
                if m {
                    j = k + 3;
                } else {
                    break;
                }
            }
            if ispix {
                linev.clear();
                if (lte.read_line(&mut linev)?) == 0 {
                    return Err(io::Error::new(ErrorKind::UnexpectedEof, ""));
                }
                var = linev.trim();
            } else {
                if skip {
                    continue 'wh;
                }
                var = &strbuf;
                break;
            }
        }
        let i: usize = var.len() - 1;
        if var.as_bytes()[0] == b'[' && matches!(var[1..].find(']'),Some(x)if (x+1)==i) {
            if var[1..].starts_with("comment_") {
                last = None;
                lastkey = None;
            } else {
                let str = &var[1..i];
                if let Some(v) = table.get_mut(str) {
                    last = Some(&mut v.0);
                } else {
                    last = None;
                }
                lastkey = Some(str.into());
            }
        } else if last.is_some() || lastkey.is_some() {
            let mut list = var.splitn(2, |c| c == ':' || c == '=');
            if let Some(key) = list.next() {
                if let Some(value) = list.next() {
                    if last.is_none() {
                        if let Some(key) = lastkey.take() {
                            last = Some(
                                &mut table
                                    .entry(Rcstr::new(Rc::new(key)))
                                    .or_insert(Section(FxHashMap::default(), None, false))
                                    .0,
                            );
                        }
                    }
                    if let Some(ref mut map) = last {
                        map.insert(
                            Rcstr::new(Rc::new(key.trim().into())),
                            Rcstr::new(Rc::new(value.trim().into())),
                        );
                    }
                }
            }
        }
    }
    Ok(table)
}
pub fn write_cnf_file(drcf: &PathBuf, map: &INICF) -> Result<(), Error> {
    let f = File::open(drcf)?;
    write_cnf_io(&mut BufWriter::new(f), map)
}
pub fn get_len<K, V>(src: &FxLinkedHashMap<K, V>) -> usize
where
    K: Borrow<str> + Hash + Eq,
    V: Borrow<FxHashMap<K, K>>,
{
    let mut size: usize = 0;
    for v in src.borrow().values() {
        let v = v.borrow();
        let vlen = v.len();
        if vlen == 0 {
            continue;
        }
        size += (vlen << 1) + 3;
        for (k2, v2) in v {
            size += k2.borrow().len();
            size += v2.borrow().len();
        }
    }
    if size > 0 { size } else { size - 1 }
}
pub fn write_cnf_io<K, V, W>(buf: &mut W, map: &FxLinkedHashMap<K, V>) -> Result<(), Error>
where
    K: Borrow<str> + Hash + Eq,
    V: Borrow<FxHashMap<K, K>>,
    W: Write,
{
    let mut ishead = false;
    for (k, v) in map {
        if v.borrow().len() == 0 {
            continue;
        }
        if ishead {
            buf.write(&[b'\n'])?;
        }
        ishead = true;
        buf.write(&[b'['])?;
        buf.write(k.borrow().as_bytes())?;
        buf.write(&[b']'])?;
        for (k2, v2) in v.borrow() {
            buf.write(&[b'\n'])?;
            buf.write(k2.borrow().as_bytes())?;
            buf.write(&[b':'])?;
            buf.write(v2.borrow().as_bytes())?;
        }
    }
    Ok(())
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
