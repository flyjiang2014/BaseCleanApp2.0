package com.flyjiang.cleanapp.callback;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.exception.HttpException;
import com.lzy.okgo.request.base.Request;
import com.flyjiang.cleanapp.base.BaseActivity;
import com.flyjiang.cleanapp.base.BaseFragmentV4;
import com.flyjiang.cleanapp.base.Constants;
import com.flyjiang.cleanapp.okgomodel.CommonReturnData;
import com.flyjiang.cleanapp.okgomodel.SimpleResponse;
import com.flyjiang.cleanapp.utils.Convert;
import com.flyjiang.cleanapp.utils.SharepreferenceUtil;
import com.flyjiang.cleanapp.utils.ToastUtil;
import com.flyjiang.cleanapp.widget.loading.LoadingLayout;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.Response;

/**
 * 网络请求callBack处理
 */
public abstract class JsonFragmentCallback<T> extends AbsCallback<T> {
    BaseFragmentV4 mFragment;
    boolean showStatue; //请求是否响应loading状态,用于展示noNetView, loadingView，emptyView,errorView
    boolean showDialog = true; //是否显示dialog

    @Override
    public void onStart(Request<T, ? extends Request> request) {
        super.onStart(request);
        if (mFragment.getActivity() != null && showDialog) {
            ((BaseActivity) (mFragment.getActivity())).getDialog().show();
        }
        request.params("token", SharepreferenceUtil.getString(Constants.TOKEN));
        request.tag(mFragment.getActivity().getClass().getSimpleName());
    }


    @Override
    public void onFinish() {
        super.onFinish();
        //网络请求结束后关闭对话框
        if (mFragment.getActivity() != null && showDialog) {
            ((BaseActivity) (mFragment.getActivity())).getDialog().dismiss();
        }
    }

    public JsonFragmentCallback(BaseFragmentV4 mFragment, boolean showStatue, boolean showDialog) {
        this.mFragment = mFragment;
        this.showStatue = showStatue;
        this.showDialog = showDialog;
    }

    /**
     * 该方法是子线程处理，不能做ui相关的工作
     * 主要作用是解析网络返回的 response 对象,生产onSuccess回调中需要的数据对象
     * 这里的解析工作不同的业务逻辑基本都不一样,所以需要自己实现,以下给出的时模板代码,实际使用根据需要修改
     */
    @Override
    public T convertResponse(Response response) throws Exception {
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        Type type = params[0];
        if (!(type instanceof ParameterizedType)) throw new IllegalStateException("没有填写泛型参数");
        Type rawType = ((ParameterizedType) type).getRawType();
        JsonReader jsonReader = new JsonReader(response.body().charStream());
        if (rawType == Void.class) {
            SimpleResponse simpleResponse = Convert.fromJson(jsonReader, SimpleResponse.class);
            response.close();
            return (T) simpleResponse.toCommonReturnData();
        } else if (rawType == CommonReturnData.class) {
            final CommonReturnData commonReturnData = Convert.fromJson(jsonReader, type);
            response.close();
            int statue = commonReturnData.getStatus();
            if (statue == 1) {
                return (T) commonReturnData;
            } else {
                    throw new IllegalStateException(commonReturnData.getMessage());
            }
        } else {
            response.close();
            throw new IllegalStateException("基类错误无法解析!");
        }
    }

    @Override
    public void onSuccess(com.lzy.okgo.model.Response<T> response) {
        if (showStatue) {
            mFragment.getmLoadingLayout().setStatus(LoadingLayout.Success);
        }
        onSuccess(response.body());
    }

    @Override
    public void onError(com.lzy.okgo.model.Response<T> response) {
        super.onError(response);
        if (mFragment.getActivity() != null && showDialog) {
            ((BaseActivity) (mFragment.getActivity())).getDialog().dismiss();//请求框架可能出现 OnError回调后,OnFinish 不回调情况，这边补充关闭 dialog.
        }
        Exception e = (Exception) response.getException();
        handleError(e);
    }

    /**
     * 部分Exception 处理
     *
     * @param e
     */
    private void handleError(Exception e) {
        if (e instanceof ConnectException) {
            //网络异常，请求超时
            ToastUtil.showToast("当前未连接到网络");
            if (showStatue && mFragment.getmLoadingLayout() != null) {
                mFragment.getmLoadingLayout().setStatus(LoadingLayout.No_Network);
            }

        } else if (e instanceof UnknownHostException) {
            // ToastUtil.showToast(e.toString());
            ToastUtil.showToast("网络服务器连接失败");
            if (showStatue && mFragment.getmLoadingLayout() != null) {
                mFragment.getmLoadingLayout().setStatus(LoadingLayout.No_Network);
            }
        } else if (e instanceof SocketException) {
            //网络异常，读取数据超时
            //   ToastUtil.showToast(e.toString());
            if (showStatue && mFragment.getmLoadingLayout() != null) {
                mFragment.getmLoadingLayout().setStatus(LoadingLayout.No_Network);
            }
        } else if (e instanceof JsonSyntaxException) {
            ToastUtil.showToast(e.toString());
        } else if (e instanceof SocketTimeoutException) {
            //  ToastUtil.showToast(e.toString());
            if (showStatue && mFragment.getmLoadingLayout() != null) {
                mFragment.getmLoadingLayout().setStatus(LoadingLayout.No_Network);
            }
        } else if (e instanceof IllegalStateException) {
            ToastUtil.showToast(e.getMessage());
        } else if (e instanceof HttpException) {
            ToastUtil.showToast(e.getMessage());
        }
    }

    public abstract void onSuccess(T t);

}