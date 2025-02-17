use std::rc::Rc;
use std::ops::Deref;
use std::cmp::Ordering;
use std::borrow::Borrow;
use std::hash::{Hash, Hasher};

#[derive(Clone, PartialEq, Eq, Hash)]
pub struct Rcstr(Rc<Box<str>>);
impl Rcstr {
    pub fn new(src: Rc<Box<str>>) -> Rcstr {
        Rcstr(src)
    }
}
impl Deref for Rcstr {
    type Target = str;
    fn deref(&self) -> &Self::Target {
        &self.0
    }
}
impl Borrow<str> for Rcstr {
    fn borrow(&self) -> &str {
        &self.0
    }
}

pub struct Strslice<'a, T: Deref<Target = str>>(T, &'a str);

impl<'a, T: Deref<Target = str>> Strslice<'a, T> {
    pub fn new(src: T, start: usize) -> Strslice<'a, T> {
        let slice = &src[start..];
        Strslice(src, slice)
    }

    pub fn new_range(src: T, start: usize, end: usize) -> Strslice<'a, T> {
        let slice = &src[start..end];
        Strslice(src, slice)
    }
}

impl<'a, T: Deref<Target = str>> Deref for Strslice<'a, T> {
    type Target = str;

    fn deref(&self) -> &Self::Target {
        self.1
    }
}

impl<'a, T: Deref<Target = str>> Borrow<str> for Strslice<'a, T> {
    fn borrow(&self) -> &str {
        self.1
    }
}

impl<'a, T: Deref<Target = str>> PartialEq for Strslice<'a, T> {
    fn eq(&self, other: &Self) -> bool {
        self.1.eq(other.1)
    }
}

impl<'a, T: Deref<Target = str>> Eq for Strslice<'a, T> {}

impl<'a, T: Deref<Target = str>> Hash for Strslice<'a, T> {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.1.hash(state);
    }
}

impl<'a, T: Deref<Target = str>> PartialOrd for Strslice<'a, T> {
    fn partial_cmp(&self, other: &Strslice<'a, T>) -> Option<Ordering> {
        self.1.partial_cmp(other.1)
    }
}

impl<'a, T: Deref<Target = str>> Ord for Strslice<'a, T> {
    fn cmp(&self, other: &Self) -> Ordering {
        self.1.cmp(other.1)
    }
}
