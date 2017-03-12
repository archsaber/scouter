// Generated by delombok at Sun Feb 26 12:31:38 KST 2017
package scouter.bytebuddy.implementation.attribute;

import scouter.bytebuddy.description.annotation.AnnotationDescription;
import scouter.bytebuddy.description.method.MethodDescription;
import scouter.bytebuddy.description.method.ParameterDescription;
import scouter.bytebuddy.description.method.ParameterList;
import scouter.bytebuddy.description.type.TypeDescription;
import scouter.bytebuddy.jar.asm.MethodVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static scouter.bytebuddy.matcher.ElementMatchers.*;

/**
 * An appender that writes attributes or annotations to a given ASM {@link MethodVisitor}.
 */
public interface MethodAttributeAppender {
    /**
     * Applies this attribute appender to a given method visitor.
     *
     * @param methodVisitor         The method visitor to which the attributes that are represented by this attribute
     * appender are written to.
     * @param methodDescription     The description of the method for which the given method visitor creates an
     * instrumentation for.
     * @param annotationValueFilter The annotation value filter to apply when the annotations are written.
     */
    void apply(MethodVisitor methodVisitor, MethodDescription methodDescription, AnnotationValueFilter annotationValueFilter);


    /**
     * A method attribute appender that does not append any attributes.
     */
    enum NoOp implements MethodAttributeAppender, Factory {
        /**
         * The singleton instance.
         */
        INSTANCE;

        @Override
        public MethodAttributeAppender make(TypeDescription typeDescription) {
            return this;
        }

        @Override
        public void apply(MethodVisitor methodVisitor, MethodDescription methodDescription, AnnotationValueFilter annotationValueFilter) {
            /* do nothing */
        }
    }


    /**
     * A factory that creates method attribute appenders for a given type.
     */
    interface Factory {
        /**
         * Returns a method attribute appender that is applicable for a given type description.
         *
         * @param typeDescription The type for which a method attribute appender is to be applied for.
         * @return The method attribute appender which should be applied for the given type.
         */
        MethodAttributeAppender make(TypeDescription typeDescription);


        /**
         * A method attribute appender factory that combines several method attribute appender factories to be
         * represented as a single factory.
         */
        class Compound implements Factory {
            /**
             * The factories this compound factory represents in their application order.
             */
            private final List<Factory> factories;

            /**
             * Creates a new compound method attribute appender factory.
             *
             * @param factory The factories that are to be combined by this compound factory in the order of their application.
             */
            public Compound(Factory... factory) {
                this(Arrays.asList(factory));
            }

            /**
             * Creates a new compound method attribute appender factory.
             *
             * @param factories The factories that are to be combined by this compound factory in the order of their application.
             */
            public Compound(List<? extends Factory> factories) {
                this.factories = new ArrayList<Factory>();
                for (Factory factory : factories) {
                    if (factory instanceof Compound) {
                        this.factories.addAll(((Compound) factory).factories);
                    } else if (!(factory instanceof NoOp)) {
                        this.factories.add(factory);
                    }
                }
            }

            @Override
            public MethodAttributeAppender make(TypeDescription typeDescription) {
                List<MethodAttributeAppender> methodAttributeAppenders = new ArrayList<MethodAttributeAppender>(factories.size());
                for (Factory factory : factories) {
                    methodAttributeAppenders.add(factory.make(typeDescription));
                }
                return new MethodAttributeAppender.Compound(methodAttributeAppenders);
            }

            @java.lang.Override
            @java.lang.SuppressWarnings("all")
            @javax.annotation.Generated("lombok")
            public boolean equals(final java.lang.Object o) {
                if (o == this) return true;
                if (!(o instanceof MethodAttributeAppender.Factory.Compound)) return false;
                final MethodAttributeAppender.Factory.Compound other = (MethodAttributeAppender.Factory.Compound) o;
                if (!other.canEqual((java.lang.Object) this)) return false;
                final java.lang.Object this$factories = this.factories;
                final java.lang.Object other$factories = other.factories;
                if (this$factories == null ? other$factories != null : !this$factories.equals(other$factories)) return false;
                return true;
            }

            @java.lang.SuppressWarnings("all")
            @javax.annotation.Generated("lombok")
            protected boolean canEqual(final java.lang.Object other) {
                return other instanceof MethodAttributeAppender.Factory.Compound;
            }

            @java.lang.Override
            @java.lang.SuppressWarnings("all")
            @javax.annotation.Generated("lombok")
            public int hashCode() {
                final int PRIME = 59;
                int result = 1;
                final java.lang.Object $factories = this.factories;
                result = result * PRIME + ($factories == null ? 43 : $factories.hashCode());
                return result;
            }
        }
    }


    /**
     * <p>
     * Implementation of a method attribute appender that writes all annotations of the instrumented method to the
     * method that is being created. This includes method and parameter annotations.
     * </p>
     * <p>
     * <b>Important</b>: This attribute appender does not apply for annotation types within the {@code jdk.internal.} namespace
     * which are silently ignored. If such annotations should be inherited, they need to be added explicitly.
     * </p>
     */
    enum ForInstrumentedMethod implements MethodAttributeAppender, Factory {
        /**
         * Appends all annotations of the instrumented method but not the annotations of the method's receiver type if such a type exists.
         */
        EXCLUDING_RECEIVER {
            @Override
            protected AnnotationAppender appendReceiver(AnnotationAppender annotationAppender, AnnotationValueFilter annotationValueFilter, MethodDescription methodDescription) {
                return annotationAppender;
            }
        },
        /**
         * <p>
         * Appends all annotations of the instrumented method including the annotations of the method's receiver type if such a type exists.
         * </p>
         * <p>
         * If a method is overridden, the annotations can be misplaced if the overriding class does not expose a similar structure to
         * the method that declared the method, i.e. the same amount of type variables and similar owner types. If this is not the case,
         * type annotations are appended as if the overridden method was declared by the original type. This does not corrupt the resulting
         * class file but it might result in type annotations not being visible via core reflection. This might however confuse other tools
         * that parse the resulting class file manually.
         * </p>
         */
        INCLUDING_RECEIVER {
            @Override
            protected AnnotationAppender appendReceiver(AnnotationAppender annotationAppender, AnnotationValueFilter annotationValueFilter, MethodDescription methodDescription) {
                TypeDescription.Generic receiverType = methodDescription.getReceiverType();
                return receiverType == null ? annotationAppender : receiverType.accept(AnnotationAppender.ForTypeAnnotations.ofReceiverType(annotationAppender, annotationValueFilter));
            }
        };

        @Override
        public MethodAttributeAppender make(TypeDescription typeDescription) {
            return this;
        }

        @Override
        public void apply(MethodVisitor methodVisitor, MethodDescription methodDescription, AnnotationValueFilter annotationValueFilter) {
            AnnotationAppender annotationAppender = new AnnotationAppender.Default(new AnnotationAppender.Target.OnMethod(methodVisitor));
            annotationAppender = methodDescription.getReturnType().accept(AnnotationAppender.ForTypeAnnotations.ofMethodReturnType(annotationAppender, annotationValueFilter));
            annotationAppender = AnnotationAppender.ForTypeAnnotations.ofTypeVariable(annotationAppender, annotationValueFilter, AnnotationAppender.ForTypeAnnotations.VARIABLE_ON_INVOKEABLE, methodDescription.getTypeVariables());
            for (AnnotationDescription annotation : methodDescription.getDeclaredAnnotations().filter(not(annotationType(nameStartsWith("jdk.internal."))))) {
                annotationAppender = annotationAppender.append(annotation, annotationValueFilter);
            }
            for (ParameterDescription parameterDescription : methodDescription.getParameters()) {
                AnnotationAppender parameterAppender = new AnnotationAppender.Default(new AnnotationAppender.Target.OnMethodParameter(methodVisitor, parameterDescription.getIndex()));
                parameterAppender = parameterDescription.getType().accept(AnnotationAppender.ForTypeAnnotations.ofMethodParameterType(parameterAppender, annotationValueFilter, parameterDescription.getIndex()));
                for (AnnotationDescription annotation : parameterDescription.getDeclaredAnnotations()) {
                    parameterAppender = parameterAppender.append(annotation, annotationValueFilter);
                }
            }
            annotationAppender = appendReceiver(annotationAppender, annotationValueFilter, methodDescription);
            int exceptionTypeIndex = 0;
            for (TypeDescription.Generic exceptionType : methodDescription.getExceptionTypes()) {
                annotationAppender = exceptionType.accept(AnnotationAppender.ForTypeAnnotations.ofExceptionType(annotationAppender, annotationValueFilter, exceptionTypeIndex++));
            }
        }

        /**
         * Appends the annotations of the instrumented method's receiver type if this is enabled and such a type exists.
         *
         * @param annotationAppender    The annotation appender to use.
         * @param annotationValueFilter The annotation value filter to apply when the annotations are written.
         * @param methodDescription     The instrumented method.
         * @return The resulting annotation appender.
         */
        protected abstract AnnotationAppender appendReceiver(AnnotationAppender annotationAppender, AnnotationValueFilter annotationValueFilter, MethodDescription methodDescription);
    }


    /**
     * Appends an annotation to a method or method parameter. The visibility of the annotation is determined by the
     * annotation type's {@link java.lang.annotation.RetentionPolicy} annotation.
     */
    class Explicit implements MethodAttributeAppender, Factory {
        /**
         * The target to which the annotations are written to.
         */
        private final Target target;
        /**
         * the annotations this method attribute appender is writing to its target.
         */
        private final List<? extends AnnotationDescription> annotations;

        /**
         * Creates a new appender for appending an annotation to a method.
         *
         * @param parameterIndex The index of the parameter to which the annotations should be written.
         * @param annotations    The annotations that should be written.
         */
        public Explicit(int parameterIndex, List<? extends AnnotationDescription> annotations) {
            this(new Target.OnMethodParameter(parameterIndex), annotations);
        }

        /**
         * Creates a new appender for appending an annotation to a method.
         *
         * @param annotations The annotations that should be written.
         */
        public Explicit(List<? extends AnnotationDescription> annotations) {
            this(Target.OnMethod.INSTANCE, annotations);
        }

        /**
         * Creates an explicit annotation appender for a either a method or one of its parameters..
         *
         * @param target      The target to which the annotation should be written to.
         * @param annotations The annotations to write.
         */
        protected Explicit(Target target, List<? extends AnnotationDescription> annotations) {
            this.target = target;
            this.annotations = annotations;
        }

        /**
         * Creates a method attribute appender factory that writes all annotations of a given method, both the method
         * annotations themselves and all annotations that are defined for every parameter.
         *
         * @param methodDescription The method from which to extract the annotations.
         * @return A method attribute appender factory for an appender that writes all annotations of the supplied method.
         */
        public static Factory of(MethodDescription methodDescription) {
            ParameterList<?> parameters = methodDescription.getParameters();
            List<MethodAttributeAppender.Factory> methodAttributeAppenders = new ArrayList<MethodAttributeAppender.Factory>(parameters.size() + 1);
            methodAttributeAppenders.add(new Explicit(methodDescription.getDeclaredAnnotations()));
            for (ParameterDescription parameter : parameters) {
                methodAttributeAppenders.add(new Explicit(parameter.getIndex(), parameter.getDeclaredAnnotations()));
            }
            return new Factory.Compound(methodAttributeAppenders);
        }

        @Override
        public MethodAttributeAppender make(TypeDescription typeDescription) {
            return this;
        }

        @Override
        public void apply(MethodVisitor methodVisitor, MethodDescription methodDescription, AnnotationValueFilter annotationValueFilter) {
            AnnotationAppender appender = new AnnotationAppender.Default(target.make(methodVisitor, methodDescription));
            for (AnnotationDescription annotation : annotations) {
                appender = appender.append(annotation, annotationValueFilter);
            }
        }


        /**
         * Represents the target on which this method attribute appender should write its annotations to.
         */
        protected interface Target {
            /**
             * Materializes the target for a given creation process.
             *
             * @param methodVisitor     The method visitor to which the attributes that are represented by this
             *                          attribute appender are written to.
             * @param methodDescription The description of the method for which the given method visitor creates an
             *                          instrumentation for.
             * @return The target of the annotation appender this target represents.
             */
            AnnotationAppender.Target make(MethodVisitor methodVisitor, MethodDescription methodDescription);

            /**
             * A method attribute appender target for writing annotations directly onto the method.
             */
            enum OnMethod implements Target {
                /**
                 * The singleton instance.
                 */
                INSTANCE;

                @Override
                public AnnotationAppender.Target make(MethodVisitor methodVisitor, MethodDescription methodDescription) {
                    return new AnnotationAppender.Target.OnMethod(methodVisitor);
                }
            }

            /**
             * A method attribute appender target for writing annotations onto a given method parameter.
             */
            class OnMethodParameter implements Target {
                /**
                 * The index of the parameter to write the annotation to.
                 */
                private final int parameterIndex;

                /**
                 * Creates a target for a method attribute appender for a method parameter of the given index.
                 *
                 * @param parameterIndex The index of the target parameter.
                 */
                protected OnMethodParameter(int parameterIndex) {
                    this.parameterIndex = parameterIndex;
                }

                @Override
                public AnnotationAppender.Target make(MethodVisitor methodVisitor, MethodDescription methodDescription) {
                    if (parameterIndex >= methodDescription.getParameters().size()) {
                        throw new IllegalArgumentException("Method " + methodDescription + " has less then " + parameterIndex + " parameters");
                    }
                    return new AnnotationAppender.Target.OnMethodParameter(methodVisitor, parameterIndex);
                }

                @java.lang.Override
                @java.lang.SuppressWarnings("all")
                @javax.annotation.Generated("lombok")
                public boolean equals(final java.lang.Object o) {
                    if (o == this) return true;
                    if (!(o instanceof MethodAttributeAppender.Explicit.Target.OnMethodParameter)) return false;
                    final MethodAttributeAppender.Explicit.Target.OnMethodParameter other = (MethodAttributeAppender.Explicit.Target.OnMethodParameter) o;
                    if (!other.canEqual((java.lang.Object) this)) return false;
                    if (this.parameterIndex != other.parameterIndex) return false;
                    return true;
                }

                @java.lang.SuppressWarnings("all")
                @javax.annotation.Generated("lombok")
                protected boolean canEqual(final java.lang.Object other) {
                    return other instanceof MethodAttributeAppender.Explicit.Target.OnMethodParameter;
                }

                @java.lang.Override
                @java.lang.SuppressWarnings("all")
                @javax.annotation.Generated("lombok")
                public int hashCode() {
                    final int PRIME = 59;
                    int result = 1;
                    result = result * PRIME + this.parameterIndex;
                    return result;
                }
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof MethodAttributeAppender.Explicit)) return false;
            final MethodAttributeAppender.Explicit other = (MethodAttributeAppender.Explicit) o;
            if (!other.canEqual((java.lang.Object) this)) return false;
            final java.lang.Object this$target = this.target;
            final java.lang.Object other$target = other.target;
            if (this$target == null ? other$target != null : !this$target.equals(other$target)) return false;
            final java.lang.Object this$annotations = this.annotations;
            final java.lang.Object other$annotations = other.annotations;
            if (this$annotations == null ? other$annotations != null : !this$annotations.equals(other$annotations)) return false;
            return true;
        }

        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        protected boolean canEqual(final java.lang.Object other) {
            return other instanceof MethodAttributeAppender.Explicit;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $target = this.target;
            result = result * PRIME + ($target == null ? 43 : $target.hashCode());
            final java.lang.Object $annotations = this.annotations;
            result = result * PRIME + ($annotations == null ? 43 : $annotations.hashCode());
            return result;
        }
    }


    /**
     * A method attribute appender that writes a receiver type.
     */
    class ForReceiverType implements MethodAttributeAppender, Factory {
        /**
         * The receiver type for which annotations are appended to the instrumented method.
         */
        private final TypeDescription.Generic receiverType;

        /**
         * Creates a new attribute appender that writes a receiver type.
         *
         * @param receiverType The receiver type for which annotations are appended to the instrumented method.
         */
        public ForReceiverType(TypeDescription.Generic receiverType) {
            this.receiverType = receiverType;
        }

        @Override
        public MethodAttributeAppender make(TypeDescription typeDescription) {
            return this;
        }

        @Override
        public void apply(MethodVisitor methodVisitor, MethodDescription methodDescription, AnnotationValueFilter annotationValueFilter) {
            receiverType.accept(AnnotationAppender.ForTypeAnnotations.ofReceiverType(new AnnotationAppender.Default(new AnnotationAppender.Target.OnMethod(methodVisitor)), annotationValueFilter));
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof MethodAttributeAppender.ForReceiverType)) return false;
            final MethodAttributeAppender.ForReceiverType other = (MethodAttributeAppender.ForReceiverType) o;
            if (!other.canEqual((java.lang.Object) this)) return false;
            final java.lang.Object this$receiverType = this.receiverType;
            final java.lang.Object other$receiverType = other.receiverType;
            if (this$receiverType == null ? other$receiverType != null : !this$receiverType.equals(other$receiverType)) return false;
            return true;
        }

        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        protected boolean canEqual(final java.lang.Object other) {
            return other instanceof MethodAttributeAppender.ForReceiverType;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $receiverType = this.receiverType;
            result = result * PRIME + ($receiverType == null ? 43 : $receiverType.hashCode());
            return result;
        }
    }


    /**
     * A method attribute appender that combines several method attribute appenders to be represented as a single
     * method attribute appender.
     */
    class Compound implements MethodAttributeAppender {
        /**
         * The method attribute appenders this compound appender represents in their application order.
         */
        private final List<MethodAttributeAppender> methodAttributeAppenders;

        /**
         * Creates a new compound method attribute appender.
         *
         * @param methodAttributeAppender The method attribute appenders that are to be combined by this compound appender
         *                                in the order of their application.
         */
        public Compound(MethodAttributeAppender... methodAttributeAppender) {
            this(Arrays.asList(methodAttributeAppender));
        }

        /**
         * Creates a new compound method attribute appender.
         *
         * @param methodAttributeAppenders The method attribute appenders that are to be combined by this compound appender
         *                                 in the order of their application.
         */
        public Compound(List<? extends MethodAttributeAppender> methodAttributeAppenders) {
            this.methodAttributeAppenders = new ArrayList<MethodAttributeAppender>();
            for (MethodAttributeAppender methodAttributeAppender : methodAttributeAppenders) {
                if (methodAttributeAppender instanceof Compound) {
                    this.methodAttributeAppenders.addAll(((Compound) methodAttributeAppender).methodAttributeAppenders);
                } else if (!(methodAttributeAppender instanceof NoOp)) {
                    this.methodAttributeAppenders.add(methodAttributeAppender);
                }
            }
        }

        @Override
        public void apply(MethodVisitor methodVisitor, MethodDescription methodDescription, AnnotationValueFilter annotationValueFilter) {
            for (MethodAttributeAppender methodAttributeAppender : methodAttributeAppenders) {
                methodAttributeAppender.apply(methodVisitor, methodDescription, annotationValueFilter);
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof MethodAttributeAppender.Compound)) return false;
            final MethodAttributeAppender.Compound other = (MethodAttributeAppender.Compound) o;
            if (!other.canEqual((java.lang.Object) this)) return false;
            final java.lang.Object this$methodAttributeAppenders = this.methodAttributeAppenders;
            final java.lang.Object other$methodAttributeAppenders = other.methodAttributeAppenders;
            if (this$methodAttributeAppenders == null ? other$methodAttributeAppenders != null : !this$methodAttributeAppenders.equals(other$methodAttributeAppenders)) return false;
            return true;
        }

        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        protected boolean canEqual(final java.lang.Object other) {
            return other instanceof MethodAttributeAppender.Compound;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $methodAttributeAppenders = this.methodAttributeAppenders;
            result = result * PRIME + ($methodAttributeAppenders == null ? 43 : $methodAttributeAppenders.hashCode());
            return result;
        }
    }
}