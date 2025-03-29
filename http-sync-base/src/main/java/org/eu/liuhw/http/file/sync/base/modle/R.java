package org.eu.liuhw.http.file.sync.base.modle;

import cn.hutool.core.date.DateTime;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;


@Getter
@Setter
public class R<T> {

    private String status;
    private String message;
    private String error;
    private String path;
    private Date timestamp;
    private T data;

    public static <U> RArray<U> ok(List<U> result) {
        final RArray<U> array = new RArray<>();
        array.setData(result);
        return ok(array);
    }

    public static <U> RObject<U> ok(U result) {
        final RObject<U> object = new RObject<>();
        object.setData(result);
        return ok(object);
    }

    public static <U extends R> U ok(U result) {
        result.setStatus("200");
        result.setMessage("success");
        result.setTimestamp(new DateTime());
        return result;
    }

}
