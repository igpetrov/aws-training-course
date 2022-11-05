package com.myorg;

import software.amazon.awscdk.CfnParameter;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.autoscaling.AutoScalingGroup;
import software.amazon.awscdk.services.autoscaling.AutoScalingGroupProps;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.MachineImage;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.Map;

public class Week1Stack extends Stack {

    private CfnParameter instanceTypeParam;
    private CfnParameter minInstances;

    public Week1Stack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public Week1Stack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        createParams();

        IVpc vpc = getDefaultVpc();
        SecurityGroup securityGroup = createSshEnabledSecurityGroup(vpc);

        AutoScalingGroup autoScalingGroup = new AutoScalingGroup(this, "ASG", AutoScalingGroupProps.builder()
                .vpc(vpc)
                .securityGroup(securityGroup)
                .desiredCapacity(minInstances.getValueAsNumber())
                .instanceType(new InstanceType(instanceTypeParam.getValueAsString()))
                .machineImage(MachineImage.genericLinux(Map.of("us-east-1", "ami-09d3b3274b6c5d4aa")))
                .keyName("mydemokey")
                .build());
        autoScalingGroup.addUserData(
                "sudo yum install -y java-11-amazon-corretto-headless"
        );
    }

    private void createParams() {
        instanceTypeParam = CfnParameter.Builder.create(this, "InstanceType")
                .type("String")
                .description("Select instance type to spawn")
                .defaultValue("t2.micro")
                .allowedValues(Arrays.asList("t1.micro", "t2.micro", "t3.micro"))
                .build();
        minInstances = CfnParameter.Builder.create(this, "MinAmount")
                .type("Number")
                .description("Select min amount of of instances")
                .defaultValue(2)
                .build();
    }

    private IVpc getDefaultVpc() {
        return Vpc.fromLookup(this, "vpc", VpcLookupOptions.builder().isDefault(true).build());
    }

    private SecurityGroup createSshEnabledSecurityGroup(IVpc vpc) {
        SecurityGroup group = SecurityGroup.Builder.create(this, "sg")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();
        group.addIngressRule(Peer.anyIpv4(), Port.tcp(22)); // allow SSH
        return group;
    }
}
