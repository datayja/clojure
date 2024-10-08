/**
 * Copyright (c) Rich Hickey. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

/* rich Apr 19, 2008 */

package clojure.lang;

import java.io.IOException;
import java.lang.ref.Reference;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.ref.ReferenceQueue;

public class Util {
    static public boolean equiv(Object k1, Object k2) {
        if (k1 == k2)
            return true;
        if (k1 != null) {
            if (k1 instanceof Number && k2 instanceof Number)
                return Numbers.equal((Number) k1, (Number) k2);
            else if (k1 instanceof IPersistentCollection || k2 instanceof IPersistentCollection)
                return pcequiv(k1, k2);
            return k1.equals(k2);
        }
        return false;
    }

    public interface EquivPred {
        boolean equiv(Object k1, Object k2);
    }

    static EquivPred equivNull = new EquivPred() {
        public boolean equiv(Object k1, Object k2) {
            return k2 == null;
        }
    };

    static EquivPred equivEquals = new EquivPred() {
        public boolean equiv(Object k1, Object k2) {
            return k1.equals(k2);
        }
    };

    static EquivPred equivNumber = new EquivPred() {
        public boolean equiv(Object k1, Object k2) {
            if (k2 instanceof Number)
                return Numbers.equal((Number) k1, (Number) k2);
            return false;
        }
    };

    static EquivPred equivColl = new EquivPred() {
        public boolean equiv(Object k1, Object k2) {
            if (k1 instanceof IPersistentCollection || k2 instanceof IPersistentCollection)
                return pcequiv(k1, k2);
            return k1.equals(k2);
        }
    };

    static public EquivPred equivPred(Object k1) {
        if (k1 == null)
            return equivNull;
        else if (k1 instanceof Number)
            return equivNumber;
        else if (k1 instanceof String || k1 instanceof Symbol)
            return equivEquals;
        else if (k1 instanceof Collection || k1 instanceof Map)
            return equivColl;
        return equivEquals;
    }

    static public boolean equiv(long k1, long k2) {
        return k1 == k2;
    }

    static public boolean equiv(Object k1, long k2) {
        return equiv(k1, (Object) k2);
    }

    static public boolean equiv(long k1, Object k2) {
        return equiv((Object) k1, k2);
    }

    static public boolean equiv(double k1, double k2) {
        return k1 == k2;
    }

    static public boolean equiv(Object k1, double k2) {
        return equiv(k1, (Object) k2);
    }

    static public boolean equiv(double k1, Object k2) {
        return equiv((Object) k1, k2);
    }

    static public boolean equiv(boolean k1, boolean k2) {
        return k1 == k2;
    }

    static public boolean equiv(Object k1, boolean k2) {
        return equiv(k1, (Object) k2);
    }

    static public boolean equiv(boolean k1, Object k2) {
        return equiv((Object) k1, k2);
    }

    static public boolean equiv(char c1, char c2) {
        return c1 == c2;
    }

    static public boolean pcequiv(Object k1, Object k2) {
        if (k1 instanceof IPersistentCollection)
            return ((IPersistentCollection) k1).equiv(k2);
        return ((IPersistentCollection) k2).equiv(k1);
    }

    static public boolean equals(Object k1, Object k2) {
        if (k1 == k2)
            return true;
        return k1 != null && k1.equals(k2);
    }

    static public boolean identical(Object k1, Object k2) {
        return k1 == k2;
    }

    static public Class classOf(Object x) {
        if (x != null)
            return x.getClass();
        return null;
    }

    static public int compare(Object k1, Object k2) {
        if (k1 == k2)
            return 0;
        if (k1 != null) {
            if (k2 == null)
                return 1;
            if (k1 instanceof Number)
                return Numbers.compare((Number) k1, (Number) k2);
            return ((Comparable) k1).compareTo(k2);
        }
        return -1;
    }

    static public int hash(Object o) {
        if (o == null)
            return 0;
        return o.hashCode();
    }

    public static int hasheq(Object o) {
        if (o == null)
            return 0;
        if (o instanceof IHashEq)
            return dohasheq((IHashEq) o);
        if (o instanceof Number)
            return Numbers.hasheq((Number) o);
        if (o instanceof String)
            return Murmur3.hashInt(o.hashCode());
        return o.hashCode();
    }

    private static int dohasheq(IHashEq o) {
        return o.hasheq();
    }

    static public int hashCombine(int seed, int hash) {
        //a la boost
        seed ^= hash + 0x9e3779b9 + (seed << 6) + (seed >> 2);
        return seed;
    }

    static public boolean isPrimitive(Class c) {
        return c != null && c.isPrimitive() && !(c == Void.TYPE);
    }

    static public boolean isInteger(Object x) {
        return x instanceof Integer
                || x instanceof Long
                || x instanceof BigInt
                || x instanceof BigInteger;
    }

    static public Object ret1(Object ret, Object nil) {
        return ret;
    }

    static public ISeq ret1(ISeq ret, Object nil) {
        return ret;
    }

    static public <K, V> void clearCache(ReferenceQueue rq, ConcurrentHashMap<K, Reference<V>> cache) {
        //cleanup any dead entries
        if (rq.poll() != null) {
            while (rq.poll() != null)
                ;
            for (Map.Entry<K, Reference<V>> e : cache.entrySet()) {
                Reference<V> val = e.getValue();
                if (val != null && val.get() == null)
                    cache.remove(e.getKey(), val);
            }
        }
    }

    static public RuntimeException runtimeException(String s) {
        return new RuntimeException(s);
    }

    static public RuntimeException runtimeException(String s, Throwable e) {
        return new RuntimeException(s, e);
    }

    /**
     * Throw even checked exceptions without being required
     * to declare them or catch them. Suggested idiom:
     * <p>
     * <code>throw sneakyThrow( some exception );</code>
     */
    static public RuntimeException sneakyThrow(Throwable t) {
        // http://www.mail-archive.com/javaposse@googlegroups.com/msg05984.html
        if (t == null)
            throw new NullPointerException();
        Util.<RuntimeException>sneakyThrow0(t);
        return null;
    }

    @SuppressWarnings("unchecked")
    static private <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
        throw (T) t;
    }

    static public Object loadWithClass(String scriptbase, Class<?> loadFrom) throws IOException, ClassNotFoundException {
        RT.init();
        Var.pushThreadBindings(RT.map(new Object[]{Compiler.LOADER, loadFrom.getClassLoader()}));
        try {
            return RT.var("clojure.core", "load").invoke(scriptbase);
        } finally {
            Var.popThreadBindings();
        }
    }

    static boolean isPosDigit(String s) {
        if (s.length() != 1)
            return false;
        char ch = s.charAt(0);
        return ch <= '9' && ch >= '1';
    }

    public static Symbol arrayTypeToSymbol(Class c) {
        int dim = 0;
        Class componentClass = c;

        while (componentClass.isArray()) {
            if (++dim > 9)
                break;

            componentClass = componentClass.getComponentType();
        }
        if (dim <= 9 && dim >= 1)
            return Symbol.intern(componentClass.getName(), Integer.toString(dim));
        else
            return Symbol.intern(null, c.getName());
    }
}

