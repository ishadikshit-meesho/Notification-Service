package com.notification.server.constants;

import java.io.Serializable;

public record BlacklistRequestBody (String[] phoneNumbers) implements Serializable{
}
