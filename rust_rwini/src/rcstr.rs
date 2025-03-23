use std::rc::Rc;
use std::ops::Deref;
use std::cmp::Ordering;
use std::borrow::Borrow;
use std::marker::PhantomData;
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
pub struct Strslice<T: Deref<Target = str>> {
    src: T,
    slice: *const str,
}
impl<T: Deref<Target = str>> Strslice<T> {
    pub fn new(src: T, start: usize) -> Self {
        let slice_ptr = &src[start..] as *const str;
        Self {
            src,
            slice: slice_ptr,
        }
    }
    pub fn new_range(src: T, start: usize, end: usize) -> Self {
        let slice_ptr = &src[start..end] as *const str;
        Self {
            src,
            slice: slice_ptr,
        }
    }
}
unsafe impl<T: Deref<Target = str> + Send> Send for Strslice<T> {}
unsafe impl<T: Deref<Target = str> + Sync> Sync for Strslice<T> {}
impl<'a, T: Deref<Target = str>> Deref for Strslice<T> {
    type Target = str;
    fn deref(&self) -> &Self::Target {
        unsafe { &*self.slice }
    }
}

impl<'a, T: Deref<Target = str>> Borrow<str> for Strslice<T> {
    fn borrow(&self) -> &str {
        (*self)
    }
}

impl<'a, T: Deref<Target = str>> PartialEq for Strslice<T> {
    fn eq(&self, other: &Self) -> bool {
        (*self).eq(*other)
    }
}

impl<'a, T: Deref<Target = str>> Eq for Strslice<T> {}

impl<'a, T: Deref<Target = str>> Hash for Strslice<T> {
    fn hash<H: Hasher>(&self, state: &mut H) {
        (*self).hash(state);
    }
}

impl<'a, T: Deref<Target = str>> PartialOrd for Strslice<T> {
    fn partial_cmp(&self, other: &Strslice<T>) -> Option<Ordering> {
        (*self).partial_cmp(*other)
    }
}

impl<'a, T: Deref<Target = str>> Ord for Strslice<T> {
    fn cmp(&self, other: &Self) -> Ordering {
        (*self).cmp(*other)
    }
}
