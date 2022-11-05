package com.myorg;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.CfnParameter;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.BlockDevice;
import software.amazon.awscdk.services.ec2.BlockDeviceVolume;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.MachineImage;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Week0Stack extends Stack {

    private CfnParameter instanceTypeParam;
    private CfnParameter ec2ImageAmi;

    public Week0Stack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public Week0Stack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);
        createParams();
        // Look up the default VPC
        IVpc vpc = getDefaultVpc();
        SecurityGroup securityGroup = createAllOutboundSecurityGroup(vpc);
        Instance instance = createInstance(vpc, securityGroup);
        new CfnOutput(this, "InstancePrivateIPAddress", CfnOutputProps.builder().value(instance.getInstancePrivateIp()).build());
        new CfnOutput(this, "InstancePublicIPAddress", CfnOutputProps.builder().value(instance.getInstancePublicIp()).build());
    }

    private void createParams() {
        instanceTypeParam = CfnParameter.Builder.create(this, "InstanceType")
                .type("String")
                .description("Select instance type to spawn")
                .defaultValue("t2.micro")
                .allowedValues(Arrays.asList("t2.micro", "t3.micro", "t4g.micro"))
                .build();
        ec2ImageAmi = CfnParameter.Builder.create(this, "AMI")
                .type("String")
                .description("Select AMI type for new instance")
                .defaultValue("ami-09d3b3274b6c5d4aa")
                .build();
    }

    private IVpc getDefaultVpc() {
        return Vpc.fromLookup(this, "vpc", VpcLookupOptions.builder().isDefault(true).build());
    }

    private SecurityGroup createAllOutboundSecurityGroup(IVpc vpc) {
        return SecurityGroup.Builder.create(this, "sg")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();
    }

    private Instance createInstance(IVpc vpc, SecurityGroup securityGroup) {
        return Instance.Builder.create(this, "instance")
                .vpc(vpc)
                .instanceType(new InstanceType(instanceTypeParam.getValueAsString()))
                // Force us-east-1
                .machineImage(MachineImage.genericLinux(Map.of("us-east-1", ec2ImageAmi.getValueAsString())))
                .blockDevices(List.of(
                        BlockDevice.builder()
                                .deviceName("/dev/sda1")
                                .volume(BlockDeviceVolume.ebs(50))
                                .build()))
                .securityGroup(securityGroup)
                .build();
    }

}
