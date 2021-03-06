/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.antlr.parser.GroovyLexer
import org.codehaus.groovy.tools.shell.Interpreter

import static org.codehaus.groovy.tools.shell.completion.TokenUtilTest.tokenList
import static org.codehaus.groovy.tools.shell.completion.TokenUtilTest.tokensString

class ReflectionCompletorTest extends GroovyTestCase {

    void testGetFieldsAndMethodsArray() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(([] as String[]), "")
        assert 'length' in result
        assert 'clone()' in result
        result = ReflectionCompletor.getMetaclassMethods(([] as String[]), "", true)
        assert 'size()' in result
        assert 'any()' in result
        result = ReflectionCompletor.getMetaclassMethods([] as String[], "size", true)
        assert ["size()"] == result
        result = ReflectionCompletor.getPublicFieldsAndMethods([] as String[], "le")
        assert ["length"] == result
    }

    void testGetFieldsAndMethodsMap() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(['id': '42'], "")
        assert 'clear()' in result
        assert 'containsKey(' in result
        assert 'clear()' in result
        // 'class' as key can cause bugs where .class is used instead of getClass()
        result = ReflectionCompletor.getPublicFieldsAndMethods(['class': '42', 'club': 53], "")
        assert 'clear()' in result
        assert 'containsKey(' in result
        assert 'clear()' in result
        assert 'class' in result
        assert 'club' in result
        result = ReflectionCompletor.getPublicFieldsAndMethods(['id': '42'], "size")
        // e.g. don't show non-public inherited size field
        assert ["size()"] == result
    }

    void testGetFieldsAndMethodsString() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods("foo", "")
        assert 'charAt(' in result
        assert 'contains(' in result
        assert 'format(' in result
        result = ReflectionCompletor.getMetaclassMethods("foo", "", true)
        assert 'normalize()' in result
        int foo = 3
        result = ReflectionCompletor.getPublicFieldsAndMethods("$foo", "")
        assert 'build(' in result
        result = ReflectionCompletor.getMetaclassMethods("foo", "tok", true)
        assert ["tokenize(", "tokenize()"] == result
        result = ReflectionCompletor.getMetaclassMethods(String, "tok", true)
        assert ["tokenize(", "tokenize()"] == result
    }

    void testGetFieldsAndMethodsPrimitive() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(3, "")
        assert "byteValue()" in result
        assert "MAX_VALUE" in result
        assert "valueOf(" in result
        assert "bitCount(" in result
        result = ReflectionCompletor.getMetaclassMethods(3, "", true)
        assert "abs()" in result
        result = ReflectionCompletor.getMetaclassMethods(3, "una", true)
        assert ["unaryMinus()", "unaryPlus()"] == result
        result = ReflectionCompletor.getMetaclassMethods(Integer, "una", true)
        assert ["unaryMinus()", "unaryPlus()"] == result
        result = ReflectionCompletor.getPublicFieldsAndMethods(Integer, "MA")
        assert ["MAX_VALUE"] == result
        result = ReflectionCompletor.getPublicFieldsAndMethods(Integer, "getI")
        assert ["getInteger("] == result
    }

    interface ForTestInterface extends Comparable {
        static final int FOR_TEST_FIELD = 1
        void forTestMethod()
    }

    void testGetFieldsAndMethodsAnonymousClass() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(new ForTestInterface(){
            @Override
            void forTestMethod() {}

            @Override
            int compareTo(Object o) {return 0}
        }, "")
        assert 'FOR_TEST_FIELD' in result
        assert 'forTestMethod()' in result
        assert 'compareTo(' in result
        GroovyLexer
        result = ReflectionCompletor.getPublicFieldsAndMethods(Set, "toA")
        assert []== result
    }

    enum ForTestEnum {
        val1, val2
        static final ForTestEnum VAL_3
        int enumMethod() {return 0}
        static int staticMethod() {return 1}
    }

    void testEnum() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(ForTestEnum, "")
        assert 'val1' in result
        assert ! ( 'enumMethod()' in result)
        assert 'staticMethod()' in result
        result = ReflectionCompletor.getPublicFieldsAndMethods(ForTestEnum.val1, "")
        // User will probably not want this
        assert ! ( 'val1' in result)
        assert 'enumMethod()' in result
        assert 'staticMethod()' in result
    }

    void testGetAbstractClassFields() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(GroovyLexer, "")
        assert 'ABSTRACT' in result
        assert 'tracing' in result
        result = ReflectionCompletor.getMetaclassMethods(GroovyLexer, "", true)
        assert 'collect()' in result
        result = ReflectionCompletor.getPublicFieldsAndMethods(new GroovyLexer(new ByteArrayInputStream()), "")
        assert 'ABSTRACT' in result
        assert 'tracing' in result
        result = ReflectionCompletor.getMetaclassMethods(new GroovyLexer(new ByteArrayInputStream()), "", true)
        assert 'isCase(' in result
        result = ReflectionCompletor.getPublicFieldsAndMethods(GroovyLexer, "LITERAL_as")
        assert ["LITERAL_as", "LITERAL_assert"]== result
        GroovyLexer lexer = new GroovyLexer(new ByteArrayInputStream("".getBytes()))
        result = ReflectionCompletor.getPublicFieldsAndMethods(lexer, "LITERAL_as")
        assert ["LITERAL_as", "LITERAL_assert"]== result
    }

    void testGetFieldsAndMethodsClass() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(Arrays, "")
        assert 'sort(' in result
        result = ReflectionCompletor.getPublicFieldsAndMethods(HashSet, "pro")
        assert []== result
        result = ReflectionCompletor.getPublicFieldsAndMethods(HashSet, "toA")
        assert []== result
        result = ReflectionCompletor.getPublicFieldsAndMethods(new HashSet(), "toA")
        assert ["toArray(", "toArray()"]== result
    }

    void testSuppressMetaAndDefaultMethods() {
        Collection<String> result = ReflectionCompletor.getMetaclassMethods("foo", "", true)
        assert 'getMetaClass()' in result
        assert 'asBoolean()' in result
        result = ReflectionCompletor.getMetaclassMethods("foo", "", false)
        assert ! ('getMetaClass()' in result)
        assert ! ('asBoolean()' in result)
    }

    void testGetFieldsAndMethodsCustomClass() {
        Interpreter interp = new Interpreter(Thread.currentThread().contextClassLoader, new Binding())
        Object instance = interp.evaluate(["class Foo extends HashSet implements Comparable {int compareTo(Object) {0}}; Foo"])
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(instance, "")
        assertFalse('compareTo(' in result)
        instance = interp.evaluate(["class Foo extends HashSet implements Comparable {int compareTo(Object) {0}}; new Foo()"])
        result = ReflectionCompletor.getPublicFieldsAndMethods(instance, "")
        assert 'compareTo(' in result
    }
}


class InvokerParsingTest extends GroovyTestCase {

    void testTokenListToEvalString() {
        assert '' == ReflectionCompletor.tokenListToEvalString(tokenList(""))
        assert '1' == ReflectionCompletor.tokenListToEvalString(tokenList("1"))
        assert '1.' == ReflectionCompletor.tokenListToEvalString(tokenList("1."))
        assert 'foo' == ReflectionCompletor.tokenListToEvalString(tokenList("foo"))
        assert '"foo"' == ReflectionCompletor.tokenListToEvalString(tokenList("'foo'"))
    }

    void testGetInvokerTokens() {
        // must make sure no token list is returned that could be evaluated with side effects.
        assert null == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('')))
        assert 'foo' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('foo')))
        assert 'bar.foo' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('bar.foo')))
        assert 'bar.&foo' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('bar.&foo')))
        // literal (simple join of token text forgets hyphens)
        assert 'foo' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('"foo"')))
        assert '1' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('1')))
        // operator
        assert 'foo' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('1+"foo"')))
        assert 'foo' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('bar == foo')))
        assert 'foo' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('bar = foo')))
        assert 'foo' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('1+foo')))
        // begin
        assert 'foo' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList(';foo')))
        assert 'foo' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('(foo')))
        assert '[foo[][2]].bar' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('[[foo[][2]].bar')))
        assert 'foo' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('${foo')))
        assert 'foo' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('"$foo')))
        assert 'foo' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('[1,foo')))
        assert 'foo' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('[1: foo')))
        // Collections
        assert '[1+2]' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('[1+2]')))
        assert '[1..2]' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('[1..2]')))
        assert '[1,2]' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('[1, 2]')))
        assert '[1:2]' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('[1: 2]')))
        // allowed Parens
        assert '((Foo)foo).' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('((Foo) foo).')))
        assert '((1+2>4)==(a&&b)).' == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('((1 + 2 > 4) == (a && b)).')))
        // not allowed
        assert null == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('foo()')))
        assert null == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('foo each')))
        assert null == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('New Foo().')))
        assert null == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('foo.each bar')))
        assert null == tokensString(ReflectionCompletor.getInvokerTokens(tokenList('foo++')))
    }
}
