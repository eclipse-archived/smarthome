/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * A human language command interpretation service.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public abstract class AbstractRuleBasedInterpreter implements HumanLanguageInterpreter {

    private static final String JSGF = "JSGF";
    private static final Set<String> supportedGrammars = Collections.unmodifiableSet(Collections.singleton(JSGF));

    private static final String OK = "ok";
    private static final String SORRY = "sorry";

    private static final String CMD = "cmd";
    private static final String NAME = "name";

    private static final String LANGUAGE_SUPPORT = "LanguageSupport";

    private HashMap<Locale, ArrayList<Rule>> languageRules;

    private ItemRegistry itemRegistry;
    private EventPublisher eventPublisher;

    private HashSet<String> identifierTokens = null;

    private RegistryChangeListener<Item> registryChangeListener = new RegistryChangeListener<Item>() {
        @Override
        public void added(Item element) {
            invalidate();
        }

        @Override
        public void removed(Item element) {
            invalidate();
        }

        @Override
        public void updated(Item oldElement, Item element) {
            invalidate();
        }
    };

    /**
     * Called whenever the rules are to be (re)generated and added by {@link addRules}
     */
    protected abstract void createRules();

    @Override
    public String interpret(Locale locale, String text) throws InterpretationException {
        ResourceBundle language = ResourceBundle.getBundle(LANGUAGE_SUPPORT, locale);
        Rule[] rules = getRules(locale);
        if (language == null || rules.length == 0) {
            throw new InterpretationException(
                locale.getDisplayLanguage(Locale.ENGLISH) + " is not supported at the moment.");
        }
        TokenList tokens = new TokenList(Arrays.asList(text.trim().toLowerCase().split("\\s++")));
        if (tokens.eof()) {
            throw new InterpretationException(language.getString(SORRY));
        }
        InterpretationResult result;

        getGrammar(locale, "JSGF");

        for (Rule rule : rules) {
            if ((result = rule.execute(language, tokens)).isSuccess()) {
                return result.getResponse();
            }
        }
        throw new InterpretationException(language.getString(SORRY));
    }

    private void invalidate() {
        identifierTokens = null;
        languageRules = null;
    }

    /**
     * All the tokens (name parts) of the names of all the items in the {@link ItemRegistry}.
     *
     * @return the identifier tokens
     */
    HashSet<String> getIdentifierTokens() {
        if (identifierTokens == null) {
            identifierTokens = new HashSet<String>();
            for (Item item : itemRegistry.getAll()) {
                identifierTokens.addAll(splitName(item.getName(), true));
            }
        }
        return identifierTokens;
    }

    /**
     * Creates an item name placeholder expression. This expression is greedy: Only use it, if there are no other
     * expressions following this one.
     * It's safer to use {@link thingRule} instead.
     *
     * @return Expression that represents a name of an item.
     */
    protected Expression name() {
        return name(null);
    }

    /**
     * Creates an item name placeholder expression. This expression is greedy: Only use it, if you are able to pass in
     * all possible stop tokens as excludes.
     * It's safer to use {@link thingRule} instead.
     *
     * @param excludes Stop tokens that will stop this expression from consuming further tokens.
     * @return Expression that represents a name of an item.
     */
    protected Expression name(HashSet<String> excludes) {
        return tag(NAME, star(new ExpressionIdentifier(this, excludes)));
    }

    private HashMap<Locale, ArrayList<Rule>> getLanguageRules() {
        if (languageRules == null) {
            languageRules = new HashMap<Locale, ArrayList<Rule>>();
            createRules();
        }
        return languageRules;
    }

    /**
     * Retrieves all {@link Rule}s to a given {@link Locale}. It also retrieves all the same-language rules into greater
     * indexes of the array (lower match priority).
     *
     * @param locale Locale filter
     * @return Rules in descending match priority order.
     */
    public Rule[] getRules(Locale locale) {
        HashMap<Locale, ArrayList<Rule>> lr = getLanguageRules();
        ArrayList<Rule> rules = new ArrayList<Rule>();
        HashSet<ArrayList<Rule>> ruleSets = new HashSet<ArrayList<Rule>>();
        ArrayList<Rule> ruleSet = lr.get(locale);
        if (ruleSet != null) {
            ruleSets.add(ruleSet);
            rules.addAll(ruleSet);
        }

        String l = locale.getLanguage();
        for (Locale rl : lr.keySet()) {
            if (rl.getLanguage().equals(l)) {
                ruleSet = lr.get(rl);
                if (!ruleSets.contains(ruleSet)) {
                    ruleSets.add(ruleSet);
                    rules.addAll(ruleSet);
                }
            }
        }
        return rules.toArray(new Rule[0]);
    }

    /**
     * Adds {@link Locale} specific rules to this interpreter. To be called from within {@link createRules}.
     *
     * @param locale Locale of the rules.
     * @param rules Rules to add.
     */
    protected void addRules(Locale locale, Rule... rules) {
        ArrayList<Rule> ruleSet = languageRules.get(locale);
        if (ruleSet == null) {
            languageRules.put(locale, ruleSet = new ArrayList<Rule>());
        }
        for (Rule rule : rules) {
            ruleSet.add(rule);
        }
    }

    /**
     * Creates an item rule on base of an expression, where the tail of the new rule's expression will consist of an
     * item
     * name expression.
     *
     * @param headExpression The head expression that should contain at least one {@link cmd} generated expression. The
     *            corresponding {@link Command} will in case of a match be sent to the matching {@link Item}.
     * @return The created rule.
     */
    protected Rule itemRule(Object headExpression) {
        return itemRule(headExpression, null);
    }

    /**
     * Creates an item rule on base of a head and a tail expression, where the middle part of the new rule's expression
     * will consist of an item
     * name expression. Either the head expression or the tail expression should contain at least one {@link cmd}
     * generated expression.
     *
     * @param headExpression The head expression.
     * @param tailExpression The tail expression.
     * @return The created rule.
     */
    protected Rule itemRule(Object headExpression, Object tailExpression) {
        Expression tail = exp(tailExpression);
        Expression expression = tail == null ? seq(headExpression, name())
                : seq(headExpression, name(tail.getFirsts()), tail);
        return new Rule(expression) {
            @Override
            public InterpretationResult interpretAST(ResourceBundle language, ASTNode node) {
                String[] name = node.findValueAsStringArray(NAME);
                ASTNode cmdNode = node.findNode(CMD);
                Object tag = cmdNode.getTag();
                Object value = cmdNode.getValue();
                Command command;
                if (tag instanceof Command) {
                    command = (Command) tag;
                } else if (value instanceof Number) {
                    command = new DecimalType(((Number) value).longValue());
                } else {
                    command = new StringType(cmdNode.getValueAsString());
                }
                if (name != null && command != null) {
                    try {
                        return new InterpretationResult(true, executeSingle(language, name, command));
                    } catch (Exception ex) {
                        return new InterpretationResult(ex);
                    }
                }
                return InterpretationResult.SEMANTIC_ERROR;
            }
        };

    }

    /**
     * Converts an object to an expression. Objects that are already instances of {@link Expression} are just returned.
     * All others are converted to {@link match} expressions.
     *
     * @param obj the object that's to be converted
     * @return resulting expression
     */
    protected Expression exp(Object obj) {
        if (obj instanceof Expression) {
            return (Expression) obj;
        } else {
            return obj == null ? null : new ExpressionMatch(obj.toString());
        }
    }

    /**
     * Converts all parameters to an expression array. Objects that are already instances of {@link Expression} are not
     * touched.
     * All others are converted to {@link match} expressions.
     *
     * @param obj the objects that are to be converted
     * @return resulting expression array
     */
    protected Expression[] exps(Object... objects) {
        ArrayList<Expression> result = new ArrayList<Expression>();
        for (int i = 0; i < objects.length; i++) {
            Expression e = exp(objects[i]);
            if (e != null) {
                result.add(e);
            }
        }
        return result.toArray(new Expression[0]);
    }

    /**
     * Adds a name to the resulting AST tree, if the given expression matches.
     *
     * @param name name to add
     * @param expression the expression that has to match
     * @return resulting expression
     */
    protected Expression tag(String name, Object expression) {
        return tag(name, expression, null);
    }

    /**
     * Adds a value to the resulting AST tree, if the given expression matches.
     *
     * @param expression the expression that has to match
     * @param tag the tag that's to be set
     * @return resulting expression
     */
    protected Expression tag(Object expression, Object tag) {
        return tag(null, expression, tag);
    }

    /**
     * Adds a name and a tag to the resulting AST tree, if the given expression matches.
     *
     * @param name name to add
     * @param expression the expression that has to match
     * @param tag the tag that's to be set
     * @return resulting expression
     */
    protected Expression tag(String name, Object expression, Object tag) {
        return new ExpressionLet(name, exp(expression), null, tag);
    }

    /**
     * Adds a command to the resulting AST tree. If the expression evaluates to a
     * numeric value, it will get a {@link DecimalType}, otherwise a {@link StringType}.
     *
     * @param expression the expression that has to match
     * @return resulting expression
     */
    protected Expression cmd(Object expression) {
        return cmd(expression, null);
    }

    /**
     * Adds a command to the resulting AST tree, if the expression matches.
     *
     * @param expression the expression that has to match
     * @param command the command that should be added
     * @return resulting expression
     */
    protected Expression cmd(Object expression, Command command) {
        return tag(CMD, expression, command);
    }

    /**
     * Creates an alternatives expression. Matches, as soon as one of the given expressions matches. They are tested in
     * the provided order. The value of the matching expression will be used for the resulting nodes's value.
     *
     * @param expressions the expressions (alternatives) that are to be tested
     * @return resulting expression
     */
    protected ExpressionAlternatives alt(Object... expressions) {
        return new ExpressionAlternatives(exps(expressions));
    }

    /**
     * Creates a sequence expression. Matches, if all the given expressions match. They are tested in
     * the provided order. The resulting nodes's value will be an {@link Object[]} that contains all values of the
     * matching expressions.
     *
     * @param expressions the expressions (alternatives) that have to match in sequence
     * @return resulting expression
     */
    protected ExpressionSequence seq(Object... expressions) {
        return new ExpressionSequence(exps(expressions));
    }

    /**
     * Creates an optional expression. Always succeeds. The resulting nodes's value will be the one of the
     * matching expression or null.
     *
     * @param expression the optionally matching expression
     * @return resulting expression
     */
    protected ExpressionCardinality opt(Object expression) {
        return new ExpressionCardinality(exp(expression), false, true);
    }

    /**
     * Creates a repeating expression that will match the given expression as often as possible. Always succeeds. The
     * resulting node's value will be an {@link Object[]} that contains all values of the
     * matches.
     *
     * @param expression the repeating expression
     * @return resulting expression
     */
    protected ExpressionCardinality star(Object expression) {
        return new ExpressionCardinality(exp(expression), false, false);
    }

    /**
     * Creates a repeating expression that will match the given expression as often as possible. Only succeeds, if there
     * is at least one match. The resulting node's value will be an {@link Object[]} that contains all values of the
     * matches.
     *
     * @param expression the repeating expression
     * @return resulting expression
     */
    protected ExpressionCardinality plus(Object expression) {
        return new ExpressionCardinality(exp(expression), true, false);
    }

    /**
     * Executes a command on one item that's to be found in the item registry by given name fragments.
     * Fails, if there is more than on item.
     *
     * @param language resource bundle used for producing localized response texts
     * @param nameFragments name fragments that are used to match an item's name.
     *            For a positive match, the item's name has to contain every fragment - independently of their order.
     *            They are treated case insensitive.
     * @param command command that should be executed
     * @return response text
     * @throws InterpretationException in case that there is no or more than on item matching the fragments
     */
    protected String executeSingle(ResourceBundle language, String[] nameFragments, Command command)
            throws InterpretationException {
        ArrayList<Item> items = getMatchingItems(nameFragments, command.getClass());
        if (items.size() < 1) {
            throw new InterpretationException(language.getString("no_objects"));
        } else if (items.size() > 1) {
            throw new InterpretationException(language.getString("multiple_objects"));
        } else {
            Item item = items.get(0);
            if (command instanceof State) {
                State newState = (State) command;
                State oldState = item.getStateAs(newState.getClass());
                if (oldState.equals(newState)) {
                    String template = language.getString("state_already_singular");
                    String cmdName = "state_" + command.toString().toLowerCase();
                    String stateText = language.getString(cmdName);
                    return template.replace("<state>", stateText);
                }
            }
            eventPublisher.post(ItemEventFactory.createCommandEvent(item.getName(), command));
            return language.getString(OK);
        }
    }

    /**
     * Filters the item registry by matching each item's name with the provided name fragments.
     * For this the item's name is at first tokenized by {@link splitName}.
     * The resulting fragments are now looked up by each and every provided fragment.
     * For the item to get included into the result list, every provided fragment has to be found among the item's ones.
     * If a command type is provided, the item also has to support it.
     *
     * @param nameFragments name fragments that are used to match an item's name.
     *            For a positive match, the item's name has to contain every fragment - independently of their order.
     *            They are treated case insensitive.
     * @param commandType optional command type that all items have to support.
     *            Provide {null} if there is no need for a certain command to be supported.
     * @return All matching items from the item registry.
     */
    protected ArrayList<Item> getMatchingItems(String[] nameFragments, Class<?> commandType) {
        ArrayList<Item> items = new ArrayList<Item>();
        for (Item item : itemRegistry.getAll()) {
            HashSet<String> parts = new HashSet<String>(splitName(item.getName(), true));
            boolean allMatch = true;
            for (String fragment : nameFragments) {
                allMatch = allMatch && parts.contains(fragment.toLowerCase());
            }
            if (allMatch && (commandType == null || item.getAcceptedCommandTypes().contains(commandType))) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Splits an item's name into single words. It splits whitespace, Pascal, Camel and Snake-casing.
     *
     * @param name the name that's to be split
     * @param toLowerCase if {true}, all resulting fragments will be made lower case
     * @return resulting fragments of the name
     */
    protected ArrayList<String> splitName(String name, boolean toLowerCase) {
        String[] split = name.split("(?<!^)(?=[A-Z])|_|\\s+");
        ArrayList<String> parts = new ArrayList<String>();
        for (int i = 0; i < split.length; i++) {
            String part = split[i].trim();
            if (part.length() > 1) {
                if (toLowerCase) {
                    part = part.toLowerCase();
                }
                parts.add(part);
            }
        }
        return parts;
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        return Collections.unmodifiableSet(getLanguageRules().keySet());
    }

    @Override
    public Set<String> getSupportedGrammarFormats() {
        return supportedGrammars;
    }

    /**
     * Helper class to generate a JSGF grammar from the rules of the interpreter.
     *
     * @author Tilman Kamp - Initial contribution and API
     *
     */
    private class JSGFGenerator {

        private Locale locale;

        private HashMap<Expression, Integer> ids = new HashMap<Expression, Integer>();
        private HashSet<Expression> exported = new HashSet<Expression>();
        private HashSet<Expression> shared = new HashSet<Expression>();
        private int counter = 0;

        private HashSet<String> identifierExcludes = new HashSet<String>();
        private HashSet<String> identifiers = new HashSet<String>();

        private StringBuilder builder = new StringBuilder();

        JSGFGenerator(Locale locale) {
            this.locale = locale;
        }

        private void addChildren(Expression exp) {
            for (Expression se : exp.getChildExpressions()) {
                addExpression(se);
            }
        }

        private int addExpression(Expression exp) {
            if (ids.containsKey(exp)) {
                shared.add(exp);
                return ids.get(exp);
            } else {
                int id = counter++;
                ids.put(exp, id);
                addChildren(exp);
                return id;
            }
        }

        private int addExportedExpression(Expression exp) {
            shared.add(exp);
            exported.add(exp);
            int id = addExpression(exp);
            return id;
        }

        private Expression unwrapLet(Expression expression) {
            while (expression instanceof ExpressionLet) {
                expression = ((ExpressionLet) expression).getSubExpression();
            }
            return expression;
        }

        private void emit(Object obj) {
            builder.append(obj);
        }

        private void emitName(Expression expression) {
            emit("r");
            emit(ids.get(unwrapLet(expression)));
        }

        private void emitReference(Expression expression) {
            emit("<");
            emitName(expression);
            emit(">");
        }

        private void emitDefinition(Expression expression) {
            if (exported.contains(expression)) {
                emit("public ");
            }
            emit("<");
            emitName(expression);
            emit("> = ");
            emitExpression(expression);
            emit(";\n\n");
        }

        private void emitUse(Expression expression) {
            if (shared.contains(expression)) {
                emitReference(expression);
            } else {
                emitExpression(expression);
            }
        }

        private void emitExpression(Expression expression) {
            expression = unwrapLet(expression);
            if (expression instanceof ExpressionMatch) {
                emitMatchExpression((ExpressionMatch) expression);
            } else if (expression instanceof ExpressionSequence) {
                emitSequenceExpression((ExpressionSequence) expression);
            } else if (expression instanceof ExpressionAlternatives) {
                emitAlternativesExpression((ExpressionAlternatives) expression);
            } else if (expression instanceof ExpressionCardinality) {
                emitCardinalExpression((ExpressionCardinality) expression);
            } else if (expression instanceof ExpressionIdentifier) {
                emitItemIdentifierExpression((ExpressionIdentifier) expression);
            }
        }

        private void emitMatchExpression(ExpressionMatch expression) {
            emit(expression.getPattern());
        }

        private void emitSequenceExpression(ExpressionSequence expression) {
            emitGroup(" ", expression.getChildExpressions());
        }

        private void emitAlternativesExpression(ExpressionAlternatives expression) {
            emitGroup(" | ", expression.getChildExpressions());
        }

        private void emitCardinalExpression(ExpressionCardinality expression) {
            if (!expression.isAtLeastOne() && !expression.isAtMostOne()) {
                emitUse(expression.getSubExpression());
                emit("*");
            } else if (expression.isAtLeastOne()) {
                emitUse(expression.getSubExpression());
                emit("+");
            } else if (expression.isAtMostOne()) {
                emit("[");
                emitUse(expression.getSubExpression());
                emit("]");
            } else {
                emitUse(expression.getSubExpression());
            }
        }

        private void emitItemIdentifierExpression(ExpressionIdentifier expression) {
            HashSet<String> remainder = new HashSet<String>(identifierExcludes);
            HashSet<String> excludes = expression.getExcludes();
            if (excludes.size() > 0) {
                remainder.removeAll(excludes);
                if (remainder.size() > 0) {
                    emit("(");
                }
                emit("<idbase>");
                for (String token : remainder) {
                    emit(" | ");
                    emit(token);
                }
                if (remainder.size() > 0) {
                    emit(")");
                }
            } else {
                emit("<idpart>");
            }
        }

        private void emitGroup(String separator, List<Expression> expressions) {
            int l = expressions.size();
            if (l > 0) {
                emit("(");
            }
            for (int i = 0; i < l; i++) {
                if (i > 0) {
                    emit(separator);
                }
                emitUse(expressions.get(i));
            }
            if (l > 0) {
                emit(")");
            }
        }

        private void emitSet(HashSet<String> set, String separator) {
            boolean sep = false;
            for (String p : set) {
                if (sep) {
                    emit(separator);
                } else {
                    sep = true;
                }
                emit(p);
            }
        }

        String getGrammar() {
            Rule[] rules = getRules(locale);
            identifiers.addAll(getIdentifierTokens());
            for (Rule rule : rules) {
                Expression e = rule.getExpression();
                addExportedExpression(e);
            }
            for (Expression e : ids.keySet()) {
                if (e instanceof ExpressionIdentifier) {
                    identifierExcludes.addAll(((ExpressionIdentifier) e).getExcludes());
                }
            }

            if (identifierExcludes.size() > 0) {
                HashSet<String> identifierBase = new HashSet<String>(identifiers);
                identifierBase.removeAll(identifierExcludes);
                emit("<idbase> = ");
                emitSet(identifierBase, " | ");
                emit(";\n\n<idpart> = <idbase> | ");
                emitSet(identifierExcludes, " | ");
                emit(";\n\n");
            } else {
                emit("<idpart> = ");
                emitSet(identifiers, " | ");
                emit(";\n\n");
            }

            for (Expression e : shared) {
                emitDefinition(e);
            }
            String grammar = builder.toString();
            return grammar;
        }

    }

    @Override
    public String getGrammar(Locale locale, String format) {
        if (format != JSGF) {
            return null;
        }
        JSGFGenerator generator = new JSGFGenerator(locale);
        return generator.getGrammar();
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        if (this.itemRegistry == null) {
            this.itemRegistry = itemRegistry;
            this.itemRegistry.addRegistryChangeListener(registryChangeListener);
        }
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        if (itemRegistry == this.itemRegistry) {
            this.itemRegistry.removeRegistryChangeListener(registryChangeListener);
            this.itemRegistry = null;
        }
    }

    public void setEventPublisher(EventPublisher eventPublisher) {
        if (this.eventPublisher == null) {
            this.eventPublisher = eventPublisher;
        }
    }

    public void unsetEventPublisher(EventPublisher eventPublisher) {
        if (eventPublisher == this.eventPublisher) {
            this.eventPublisher = null;
        }
    }

}
