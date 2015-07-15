package org.dsa.iot.dslink.node.actions;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.SubscriptionManager;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;

/**
 * Action API for handling invocations, parameters, and results.
 *
 * @author Samuel Grenier
 */
public class Action {

    private JsonArray params = new JsonArray();
    private JsonArray results = new JsonArray();

    private SubscriptionManager manager;
    private Node node;

    private Permission permission;
    private ResultType resultType;
    private boolean hidden;

    private final Handler<ActionResult> handler;

    /**
     * The default result type is {@link ResultType#VALUES}. This value must
     * be changed accordingly.
     *
     * @param permission Minimum required permission to invoke.
     * @param handler    Handler for invocation. Allowed to be {@code null}
     *                   for profiles.
     */
    public Action(Permission permission,
                  Handler<ActionResult> handler) {
        if (permission == null)
            throw new NullPointerException("permission");
        this.resultType = ResultType.VALUES;
        this.permission = permission;
        this.handler = handler;
    }

    /**
     * Used to the action as hidden from DSA. This allows the creation of
     * action profiles
     *
     * @param hidden Whether to hide the action or not.
     */
    @SuppressWarnings("unused")
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Updates the permission level of the action.
     *
     * @param permission New permission level
     */
    public void setPermission(Permission permission) {
        if (permission == null)
            throw new NullPointerException("permission");
        this.permission = permission;
    }

    /**
     * The default result type {@link ResultType#VALUES} and must be
     * set accordingly.
     *
     * @param type The new result type to set.
     * @return Current object for daisy chaining.
     */
    public Action setResultType(ResultType type) {
        if (type == null)
            throw new NullPointerException("type");
        this.resultType = type;
        return this;
    }

    /**
     * @param parameter Add a parameter for the invocation
     * @return Current object for daisy chaining
     */
    public Action addParameter(Parameter parameter) {
        if (parameter == null) {
            throw new NullPointerException("parameter");
        }
        JsonObject param = paramToJson(parameter);
        if (param != null) {
            params.addObject(param);
        }
        return this;
    }

    /**
     * @param parameter Add a result for the invocation
     * @return Current object for daisy chaining;
     */
    public Action addResult(Parameter parameter) {
        if (parameter == null) {
            throw new NullPointerException("parameter");
        } else if (parameter.getDefault() != null) {
            String err = "Parameter cannot contain a default value in a result";
            throw new IllegalStateException(err);
        } else if (parameter.getEditorType() != null) {
            String err = "Parameter cannot contain an editor type";
            throw new IllegalStateException(err);
        }
        JsonObject result = paramToJson(parameter);
        if (result != null) {
            results.addObject(result);
        }
        return this;
    }

    /**
     * Clears the existing parameters and adds the new parameters.
     *
     * @param newParams Parameters to set.
     */
    public void setParams(List<Parameter> newParams) {
        this.params = new JsonArray();
        for (Parameter p : newParams) {
            addParameter(p);
        }
        if (node != null && manager != null) {
            Value v = new Value(getParams());
            manager.postMetaUpdate(node, "$params", v);
        }
    }

    public void setSubscriptionManager(Node node,
                                       SubscriptionManager manager) {
        this.node = node;
        this.manager = manager;
    }

    /**
     * Invokes the action.
     *
     * @param result Result to populate as a result of invocation
     */
    public void invoke(ActionResult result) {
        if (!hasPermission()) return;
        handler.handle(result);
    }

    /**
     * Hidden actions are used for action profiles.
     *
     * @return Whether the action is hidden or not.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * @return Whether the user has permission to invoke
     */
    public boolean hasPermission() {
        return permission != Permission.NONE;
    }

    /**
     * @return Permission level of this action.
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * @return The result type this invocation returns.
     */
    public ResultType getResultType() {
        return resultType;
    }

    /**
     * @return Parameters of the action.
     */
    public JsonArray getParams() {
        return params;
    }

    /**
     * @return The columns of the action
     */
    public JsonArray getColumns() {
        return results;
    }

    /**
     * Converts all the parameters to JSON consumable format.
     *
     * @param param Parameter to convert.
     * @return JSON object of the converted parameter.
     */
    private JsonObject paramToJson(Parameter param) {
        if (param == null)
            return null;
        JsonObject obj = new JsonObject();
        obj.putString("name", param.getName());
        obj.putString("type", param.getType().toJsonString());
        Value defVal = param.getDefault();
        if (defVal != null) {
            ValueUtils.toJson(obj, "default", defVal);
        }

        EditorType type = param.getEditorType();
        if (type != null) {
            obj.putString("editor", type.toJsonString());
        }

        String description = param.getDescription();
        if (description != null) {
            obj.putString("description", description);
        }

        String placeHolder = param.getPlaceHolder();
        if (placeHolder != null) {
            obj.putString("placeholder", placeHolder);
        }

        return obj;
    }
}
