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
    _marker: PhantomData<*const str>,
}
impl<T: Deref<Target = str>> Strslice<T> {
    pub fn new(src: T, start: usize) -> Self {
        let slice_ptr = &src[start..] as *const str;
        Self {
            src,
            slice: slice_ptr,
            _marker: PhantomData,
        }
    }
    pub fn new_range(src: T, start: usize, end: usize) -> Self {
        let slice_ptr = &src[start..end] as *const str;
        Self {
            src,
            slice: slice_ptr,
            _marker: PhantomData,
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
        unsafe { &*self.slice }
    }
}

impl<'a, T: Deref<Target = str>> PartialEq for Strslice<T> {
    fn eq(&self, other: &Self) -> bool {
        (unsafe { &*self.slice }).eq(unsafe { &*other.slice })
    }
}

impl<'a, T: Deref<Target = str>> Eq for Strslice<T> {}

impl<'a, T: Deref<Target = str>> Hash for Strslice<T> {
    fn hash<H: Hasher>(&self, state: &mut H) {
        (unsafe { &*self.slice }).hash(state);
    }
}

impl<'a, T: Deref<Target = str>> PartialOrd for Strslice<T> {
    fn partial_cmp(&self, other: &Strslice<T>) -> Option<Ordering> {
        (unsafe { &*self.slice }).partial_cmp(unsafe { &*other.slice })
    }
}

impl<'a, T: Deref<Target = str>> Ord for Strslice<T> {
    fn cmp(&self, other: &Self) -> Ordering {
        (unsafe { &*self.slice }).cmp(unsafe { &*other.slice })
    }
}
