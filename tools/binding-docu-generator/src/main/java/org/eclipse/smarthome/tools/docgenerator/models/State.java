package org.eclipse.smarthome.tools.docgenerator.models;

import org.eclipse.smarthome.tools.docgenerator.data.OptionList;

import java.math.BigDecimal;

public class State implements Model<org.eclipse.smarthome.tools.docgenerator.schemas.State> {
    /**
     * Instance from the XML parser.
     */
    private org.eclipse.smarthome.tools.docgenerator.schemas.State delegate;

    /**
     * Default constructor.
     */
    public State() {
    }

    /**
     * Constructor.
     *
     * @param delegate The original instance from the XML parser.
     */
    public State(org.eclipse.smarthome.tools.docgenerator.schemas.State delegate) {
        this.delegate = delegate;
    }

    /**
     * @return Instance from the XML parser.
     */
    @Override
    public org.eclipse.smarthome.tools.docgenerator.schemas.State getRealImpl() {
        return delegate;
    }

    /**
     * Set the model.
     *
     * @param state Instance from the XML parser.
     */
    @Override
    public void setModel(org.eclipse.smarthome.tools.docgenerator.schemas.State state) {
        this.delegate = state;
    }

    /**
     * @return Whether the state is readonly.
     */
    public Boolean readOnly() {
        return delegate.isReadOnly();
    }

    /**
     * @return The minimal value of the state.
     */
    public BigDecimal min() {
        return delegate.getMin();
    }

    /**
     * @return The maximal value of the state.
     */
    public BigDecimal max() {
        return delegate.getMax();
    }

    /**
     * @return The step between the values of the state.
     */
    public BigDecimal step() {
        return delegate.getStep();
    }

    /**
     * @return The pattern for the state.
     */
    public String pattern() {
        return delegate.getPattern();
    }

    /**
     * @return A list of options.
     */
    public OptionList options() {
        OptionList optionList = new OptionList();
        if (delegate.getOptions() != null) {
            for (org.eclipse.smarthome.tools.docgenerator.schemas.Option option : delegate.getOptions().getOption()) {
                optionList.put(option);
            }
        }
        return optionList;
    }

}
