from rest_framework import serializers

from users.admin import User
from .models import Job, Address, JobItem, JobTech
from part.models import Part

from rest_framework import serializers
from .models import Job, JobItem, Address

class AddressSerializer(serializers.ModelSerializer):
    class Meta:
        model = Address
        fields = '__all__'


class JobItemSerializer(serializers.ModelSerializer):
    """Serializer for JobItem model."""
    job = serializers.PrimaryKeyRelatedField(queryset=Job.objects.all())
    stock_item = serializers.PrimaryKeyRelatedField(queryset=Part.objects.all())

    class Meta:
        model = JobItem
        fields = ['id', 'job', 'stock_item', 'quantity']


class JobSerializer(serializers.ModelSerializer):
    address = AddressSerializer()
    items = JobItemSerializer(many=True, read_only=True)

    class Meta:
        model = Job
        fields = ['job_id', 'name', 'address', 'status', 'description', 'items']

    def create(self, validated_data):
        address_data = validated_data.pop('address')
        address = Address.objects.create(**address_data)
        job = Job.objects.create(address=address, **validated_data)
        return job

class JobItemListSerializer(serializers.ListSerializer):
    """ListSerializer for updating multiple JobItems."""
    child = JobItemSerializer()

    def update(self, instance, validated_data):
        job_mapping = {item.id: item for item in instance}
        data_mapping = {item['id']: item for item in validated_data}

        ret = []
        for job_id, data in data_mapping.items():
            job_item = job_mapping.get(job_id, None)
            if job_item is not None:
                print("Updating job item", job_item.id)
                ret.append(self.child.update(job_item, data))
            else:
                print("Creating new job item")
                ret.append(self.child.create(data))

        return ret
    
class JobTechSerializer(serializers.ModelSerializer):
    """Serializer for JobTech model."""
    job = serializers.PrimaryKeyRelatedField(queryset=Job.objects.all())
    technician = serializers.PrimaryKeyRelatedField(queryset=User.objects.all())

    class Meta:
        model = JobTech
        fields = ['id', 'job', 'technician']