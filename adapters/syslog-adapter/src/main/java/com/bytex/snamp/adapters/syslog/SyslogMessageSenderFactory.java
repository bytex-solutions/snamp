package com.bytex.snamp.adapters.syslog;

import com.cloudbees.syslog.MessageFormat;
import com.cloudbees.syslog.sender.SyslogMessageSender;
import com.cloudbees.syslog.sender.TcpSyslogMessageSender;
import com.cloudbees.syslog.sender.UdpSyslogMessageSender;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
enum SyslogMessageSenderFactory {
    TCP {
        @Override
        TcpSyslogMessageSender create(final String address,
                                      final int port,
                                      final MessageFormat format,
                                      final boolean ssl,
                                      final int connectionTimeout) {
            final TcpSyslogMessageSender sender = new TcpSyslogMessageSender();
            sender.setSsl(ssl);
            sender.setMessageFormat(format);
            sender.setSyslogServerPort(port);
            sender.setSyslogServerHostname(address);
            sender.setSocketConnectTimeoutInMillis(connectionTimeout);
            return sender;
        }
    },
    UDP {
        @Override
        UdpSyslogMessageSender create(final String address,
                                   final int port,
                                   final MessageFormat format,
                                   final boolean ssl,
                                   final int connectionTimeout) {
            final UdpSyslogMessageSender sender = new UdpSyslogMessageSender();
            sender.setMessageFormat(format);
            sender.setSyslogServerPort(port);
            sender.setSyslogServerHostname(address);
            return sender;
        }
    };

    abstract SyslogMessageSender create(final String address,
                               final int port,
                               final MessageFormat format,
                               final boolean ssl,
                               final int connectionTimeout);
}
