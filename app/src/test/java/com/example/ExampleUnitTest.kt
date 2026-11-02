package com.example

import com.example.data.model.InvestmentPackageEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPackagesConfiguration() {
        val packages = listOf(
            InvestmentPackageEntity("starter", "Starter", 50.0, 50.0, 7, 20.0, "Starter package"),
            InvestmentPackageEntity("basic", "Basic", 100.0, 100.0, 7, 20.0, "Basic package"),
            InvestmentPackageEntity("standard", "Standard", 250.0, 250.0, 7, 20.0, "Standard package"),
            InvestmentPackageEntity("premium", "Premium", 500.0, 500.0, 7, 20.0, "Premium package"),
            InvestmentPackageEntity("vip", "VIP", 1000.0, 1000.0, 7, 20.0, "VIP package")
        )

        assertEquals(5, packages.size)
        assertEquals(50.0, packages[0].minInvestment, 0.001)
        assertEquals(100.0, packages[1].minInvestment, 0.001)
        assertEquals(250.0, packages[2].minInvestment, 0.001)
        assertEquals(500.0, packages[3].minInvestment, 0.001)
        assertEquals(1000.0, packages[4].minInvestment, 0.001)

        packages.forEach { pkg ->
            assertEquals(7, pkg.durationDays)
            assertEquals(20.0, pkg.targetProfitPercent, 0.001)
        }
    }

    @Test
    fun testProfitDistributionMath() {
        val investedCapital = 250.0
        val targetProfitPercent = 20.0
        val targetReturn = investedCapital * (targetProfitPercent / 100.0)
        assertEquals(50.0, targetReturn, 0.001)

        val verifiedSessionReturnPercent = 14.5
        val actualReturn = investedCapital * (verifiedSessionReturnPercent / 100.0)
        assertEquals(36.25, actualReturn, 0.001)

        val drawdownPercent = -6.0
        val lossReturn = investedCapital * (drawdownPercent / 100.0)
        assertEquals(-15.0, lossReturn, 0.001)
    }

    @Test
    fun testInvestorIdFormat() {
        val randomNum = 849201
        val investorId = "INV-$randomNum"
        assertTrue(investorId.startsWith("INV-"))
        assertEquals(10, investorId.length)
    }

    @Test
    fun testCycleDurationDaysToMillis() {
        val days = 7
        val millisInDay = 24L * 60 * 60 * 1000
        val totalMillis = days * millisInDay
        assertEquals(604800000L, totalMillis)
    }
}
