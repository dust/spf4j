/*
 * Copyright (c) 2001, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.spf4j.zel.instr;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import org.spf4j.zel.vm.AssignableValue;
import org.spf4j.zel.vm.ExecutionContext;
import org.spf4j.zel.vm.Method;
import org.spf4j.zel.vm.Program;
import org.spf4j.zel.vm.SuspendedException;
import org.spf4j.zel.vm.ZExecutionException;

/**
 *
 * @author zoly
 */
public final class CALLREF extends Instruction {

    private static final long serialVersionUID = 759722625722456554L;

    private CALLREF() {
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("ITC_INHERITANCE_TYPE_CHECKING")
    public int execute(final ExecutionContext context)
            throws ZExecutionException, InterruptedException, SuspendedException {
        final Integer nrParams = (Integer) context.pop();
        final Object[] parameters;
        try {
            parameters = context.popSyncStackVals(nrParams);
        } catch (SuspendedException e) {
            context.push(nrParams);
            throw e;
        }
        final Object function = ((AssignableValue) context.pop()).get();

        context.push(new AssignableValue() {

            @Override
            public void assign(final Object object) throws ZExecutionException, InterruptedException {
                context.resultCache.putPermanentResult((Program) function,
                        Arrays.asList(parameters), object);

            }

            @Override
            public Object get() throws ZExecutionException, InterruptedException {

                if (function instanceof Program) {
                    final Program p = (Program) function;
                    final ExecutionContext nctx;
                    Object obj;
                    switch (p.getType()) {
                        case DETERMINISTIC:
                            nctx = context.getSyncSubProgramContext(p, parameters);
                            obj = context.resultCache.getResult(p, Arrays.asList(parameters), new Callable<Object>() {
                                @Override
                                public Object call() throws Exception {
                                    return Program.executeSync(nctx);
                                }
                            });

                            break;
                        case NONDETERMINISTIC:
                            nctx = context.getSyncSubProgramContext(p, parameters);
                            obj = Program.executeSync(nctx);
                            break;
                        default:
                            throw new UnsupportedOperationException(p.getType().toString());
                    }
                    return obj;
                } else if (function instanceof Method) {
                    try {
                        return ((Method) function).invoke(context, parameters);
                    } catch (IllegalAccessException ex) {
                        throw new ZExecutionException("cannot invoke " + function, ex);
                    } catch (InvocationTargetException ex) {
                        throw new ZExecutionException("cannot invoke " + function, ex);
                    } catch (Exception ex) {
                        throw new ZExecutionException("cannot invoke " + function, ex);
                    }
                } else {
                    throw new ZExecutionException("cannot invoke " + function);
                }

            }
        });

        return 1;
    }

    /**
     * instance
     */
    public static final Instruction INSTANCE = new CALLREF();

    @Override
    public Object[] getParameters() {
        return org.spf4j.base.Arrays.EMPTY_OBJ_ARRAY;
    }
}
