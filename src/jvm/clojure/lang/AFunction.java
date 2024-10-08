/**
 * Copyright (c) Rich Hickey. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

/* rich Dec 16, 2008 */

package clojure.lang;

import java.io.Serializable;
import java.util.Comparator;

public abstract class AFunction extends AFn implements IObj, Comparator, Fn, Serializable {

    private static final long serialVersionUID = 4469383498184457675L;

    public volatile MethodImplCache __methodImplCache;

    public IPersistentMap meta() {
        return null;
    }

    public IObj withMeta(final IPersistentMap meta) {
        if (meta == null)
            return this;
        return new RestFn() {
            protected Object doInvoke(Object args) {
                return AFunction.this.applyTo((ISeq) args);
            }

            public IPersistentMap meta() {
                return meta;
            }

            public IObj withMeta(IPersistentMap newMeta) {
                if (meta == newMeta)
                    return this;
                return AFunction.this.withMeta(newMeta);
            }

            public int getRequiredArity() {
                return 0;
            }
        };
    }

    public int compare(Object o1, Object o2) {
        Object o = invoke(o1, o2);

        if (o instanceof Boolean) {
            if (RT.booleanCast(o))
                return -1;
            return RT.booleanCast(invoke(o2, o1)) ? 1 : 0;
        }

        Number n = (Number) o;
        return n.intValue();
    }
}
