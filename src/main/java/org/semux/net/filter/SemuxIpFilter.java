/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.net.filter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.semux.net.filter.exception.IpFilterJsonParseException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.netty.handler.ipfilter.IpFilterRuleType;

/**
 * SemuxIpFilter is responsible for matching IP address of incoming connection
 * against defined rules in ipconfig.json
 *
 * Example Definition of Blacklisting IP Addresses:
 * <p>
 * <blockquote>
 *
 * <pre>
 *     {
 *         "rules": [
 *             {"type": "REJECT", "address": "1.2.3.4"},
 *             {"type": "REJECT", "address": "5.6.7.8"}
 *         ]
 *     }
 * </pre>
 *
 * </blockquote>
 * </p>
 * Example Definition of Whitelisting Local Networks:
 * <p>
 * <blockquote>
 *
 * <pre>
 *     {
 *         "rules": [
 *             {"type": "ACCEPT", "address": "127.0.0.1/8"},
 *             {"type": "ACCEPT", "address": "192.168.0.0/16"},
 *             {"type": "REJECT", "address": "0.0.0.0/0"}
 *         ]
 *     }
 * </pre>
 *
 * </blockquote>
 * </p>
 */
public class SemuxIpFilter {

    private static final Logger logger = Logger.getLogger(SemuxIpFilter.class.getName());

    /**
     * The default name of ipfilter config file.
     */
    public static final String CONFIG_FILE = "ipfilter.json";

    /**
     * CopyOnWriteArrayList allows APIs to update rules atomically without affecting
     * the performance of read-only iteration
     */
    private final CopyOnWriteArrayList<FilterRule> rules;

    public SemuxIpFilter(List<FilterRule> rules) {
        this.rules = new CopyOnWriteArrayList<>(rules);
    }

    public SemuxIpFilter() {
        this.rules = new CopyOnWriteArrayList<>();
    }

    public List<FilterRule> getRules() {
        return rules;
    }

    /**
     * isAcceptable method matches supplied address against defined rules
     * sequentially and returns a result based on the first matched rule's type
     *
     * @param address
     *            an address which will be matched against defined rules
     * @return whether the address is blocked or not
     */
    public boolean isAcceptable(InetSocketAddress address) {
        return rules.stream().filter(rule -> rule != null && rule.matches(address)).findFirst().flatMap(rule -> {
            if (rule.ruleType() == IpFilterRuleType.ACCEPT) {
                return Optional.of(true);
            } else {
                return Optional.of(false);
            }
        }).orElse(true);
    }

    /**
     * Block a single IP at runtime
     *
     * @param ip
     *            The IP address to be blacklisted
     * @throws UnknownHostException
     */
    public void blacklistIp(String ip) throws UnknownHostException {
        // prepend a REJECT IP rule to the rules list to ensure that the IP will be
        // blocked
        FilterRule rule = new FilterRule(ip, IpFilterRuleType.REJECT);
        rules.remove(rule); // remove duplicated rule
        rules.add(0, rule); // prepend rule
        logger.info(String.format("Blacklisted IP %s", ip));
    }

    /**
     * Whitelist a single IP at runtime
     *
     * @param ip
     *            The IP address to be whitelisted
     * @throws UnknownHostException
     */
    public void whitelistIp(String ip) throws UnknownHostException {
        // prepend an ACCEPT IP rule to the rules list to ensure that the IP will be
        // accepted
        FilterRule rule = new FilterRule(ip, IpFilterRuleType.ACCEPT);
        rules.remove(rule); // remove duplicated rule
        rules.add(0, rule); // prepend rule
        logger.info(String.format("Whitelisted IP %s", ip));
    }

    /**
     * Append a rule to the rear of rules list
     *
     * @param rule
     *            The rule to be appended
     */
    public void appendRule(FilterRule rule) {
        rules.add(rule);
    }

    /**
     * Remove all rules
     */
    public void purgeRules() {
        rules.clear();
    }

    /**
     * Persist rules into target path.
     *
     * @param path
     *            the path where rules will be persisted at.
     */
    public void persist(Path path) {
        new Saver().save(path, this);
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public static SemuxIpFilter jsonCreator(
            @JsonProperty(value = "rules", required = true) List<FilterRule> rules) {
        return new SemuxIpFilter(rules);
    }

    /**
     * Builder is an object builder of SemuxIpFilter.
     * <p>
     * <blockquote>
     *
     * <pre>
     * SemuxIpFilter ipFilter = new Builder().accept("127.0.0.1").accept("192.168.0.0/16").reject("0.0.0.0/0").build();
     * </pre>
     *
     * </blockquote>
     * </p>
     * is equivalent to the definition of:
     * <p>
     * <blockquote>
     *
     * <pre>
     *     {
     *         "rules": [
     *             {"type": "ACCEPT", "address": "127.0.0.1/8"},
     *             {"type": "ACCEPT", "address": "192.168.0.0/16"},
     *             {"type": "REJECT", "address": "0.0.0.0/0"}
     *         ]
     *     }
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    public static final class Builder {

        private final ArrayList<FilterRule> rules = new ArrayList<>();

        private void addRule(String cidrNotation, IpFilterRuleType type) throws UnknownHostException {
            FilterRule ipSubnetFilterRule = new FilterRule(cidrNotation, type);
            rules.add(ipSubnetFilterRule);
        }

        public Builder accept(String cidrNotation) throws UnknownHostException {
            addRule(cidrNotation, IpFilterRuleType.ACCEPT);
            return this;
        }

        public Builder reject(String cidrNotation) throws UnknownHostException {
            addRule(cidrNotation, IpFilterRuleType.REJECT);
            return this;
        }

        public List<FilterRule> getRules() {
            return rules;
        }

        public SemuxIpFilter build() {
            return new SemuxIpFilter(rules);
        }
    }

    /**
     * ${@link Loader} is responsible for loading ipfilter.json file into an
     * instance of SemuxIpFilter.
     */
    public static final class Loader {

        public SemuxIpFilter load(Path path) {
            try {
                if (path.toFile().exists()) {
                    return new ObjectMapper().readValue(path.toFile(),
                            SemuxIpFilter.class);
                } else {
                    return new SemuxIpFilter();
                }
            } catch (IOException e) {
                throw new IpFilterJsonParseException(String.format(
                        "Failed to parse %s. The file may be corrupted. Please either remove it or create a valid JSON file.",
                        path.toAbsolutePath().toString()), e);
            }
        }
    }

    /**
     * ${@link Saver} is responsible for persisting the state of a
     * ${@link SemuxIpFilter} instance.
     */
    public static final class Saver {

        public void save(Path path, SemuxIpFilter ipFilter) {
            try {
                if (!path.getParent().toFile().exists()) {
                    Files.createDirectories(path.getParent());
                }

                new ObjectMapper().writer(SerializationFeature.INDENT_OUTPUT).writeValue(path.toFile(), ipFilter);
            } catch (IOException e) {
                logger.severe(String.format("Failed to save ip filter: {}", e.getMessage()));
            }
        }

    }
}
