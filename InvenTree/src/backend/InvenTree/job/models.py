"""Job database model definitions."""

from django.db import models
from django.urls import reverse
from django.conf import settings
from django.utils.translation import gettext_lazy as _

class Address(models.Model):
    """Model representing an address for a job."""
    street = models.CharField(max_length=255, verbose_name=_('Street'))
    city = models.CharField(max_length=100, verbose_name=_('City'))
    state = models.CharField(max_length=100, verbose_name=_('State'))
    zip_code = models.CharField(max_length=20, verbose_name=_('Zip Code'))
    country = models.CharField(max_length=100, verbose_name=_('Country'))

    def __str__(self):
        return f"{self.street}, {self.city}, {self.state}, {self.zip_code}, {self.country}"

class Job(models.Model):
    """Model representing a job."""
    job_id = models.AutoField(primary_key=True)
    name = models.CharField(max_length=255, verbose_name=_('Job Name'))
    address = models.OneToOneField(Address, on_delete=models.CASCADE, verbose_name=_('Address'))
    status = models.ForeignKey(
        'JobStatus',
        on_delete=models.CASCADE,
        verbose_name=_('Status'),
        default=1
    )
    description = models.TextField(blank=True, verbose_name=_('Description'))

    def __str__(self):
        return self.name

    def get_absolute_url(self):
        return reverse('job-detail', kwargs={'pk': self.job_id})

class JobStatus(models.Model):
    """Model representing a job status."""
    name = models.CharField(max_length=100, verbose_name=_('Name'))
    description = models.TextField(blank=True, verbose_name=_('Description'))

    def __str__(self):
        return self.name

class JobItem(models.Model):
    """Model representing an item moved from stock to a job."""
    job = models.ForeignKey(Job, on_delete=models.CASCADE, related_name='items', verbose_name=_('Job'))
    stock_item = models.ForeignKey('part.Part', on_delete=models.CASCADE, verbose_name=_('Stock Item'))
    quantity = models.DecimalField(max_digits=15, decimal_places=5, verbose_name=_('Quantity'))

    def __str__(self):
        return f"{self.quantity} of {self.stock_item} for {self.job}"
    
class JobTech(models.Model):
    """Model representing a technician assigned to a job."""
    job = models.ForeignKey(Job, on_delete=models.CASCADE, related_name='techs', verbose_name=_('Job'))
    technician = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, verbose_name=_('Technician'))

    def __str__(self):
        return f"{self.technician} assigned to {self.job}"